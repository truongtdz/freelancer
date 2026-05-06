package com.freelancer.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelancer.config.VNPayConfig;
import com.freelancer.entity.Contract;
import com.freelancer.entity.Transaction;
import com.freelancer.entity.enums.ContractStatus;
import com.freelancer.entity.enums.PaymentMethod;
import com.freelancer.entity.enums.TransactionStatus;
import com.freelancer.entity.enums.TransactionType;
import com.freelancer.event.EventPublisher;
import com.freelancer.event.PaymentReceivedEvent;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.repository.ContractRepository;
import com.freelancer.repository.JobRepository;
import com.freelancer.repository.TransactionRepository;
import com.freelancer.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;
    private final ContractRepository contractRepository;
    private final TransactionRepository transactionRepository;
    private final JobRepository jobRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public String createPaymentUrl(Long contractId, Long clientId, HttpServletRequest request) {
        Contract contract = contractRepository.findByIdAndDeletedAtIsNull(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        if (!contract.getClientId().equals(clientId)) {
            throw new AppException(ErrorCode.CONTRACT_NOT_PARTICIPANT);
        }
        if (contract.getStatus() != ContractStatus.PENDING_PAYMENT) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Hợp đồng không ở trạng thái chờ thanh toán");
        }

        String vnpTxnRef = VNPayUtil.generateTxnRef(contractId);
        String transactionCode = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        // Tạo transaction PENDING
        Transaction txn = Transaction.builder()
                .transactionCode(transactionCode)
                .contractId(contractId)
                .userId(clientId)
                .type(TransactionType.DEPOSIT)
                .amount(contract.getAgreedPrice())
                .currency("VND")
                .status(TransactionStatus.PENDING)
                .paymentMethod(PaymentMethod.VNPAY)
                .vnpTxnRef(vnpTxnRef)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(txn);

        // Build VNPay params
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(vnPayConfig.getExpireMinutes());

        long amountVnd = contract.getAgreedPrice().multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountVnd));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", vnpTxnRef);
        params.put("vnp_OrderInfo", "Thanh toan HD " + contract.getContractCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr", VNPayUtil.getClientIp(request));
        params.put("vnp_CreateDate", VNPayUtil.formatDate(now));
        params.put("vnp_ExpireDate", VNPayUtil.formatDate(expireTime));

        String queryString = VNPayUtil.buildQueryString(params, vnPayConfig.getHashSecret());
        String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryString;

        log.info("Created VNPay payment URL for contract={}, txnRef={}", contractId, vnpTxnRef);
        return paymentUrl;
    }

    @Override
    @Transactional
    public Map<String, String> handleIpn(Map<String, String> params) {
        if (!VNPayUtil.verifySignature(params, vnPayConfig.getHashSecret())) {
            log.warn("VNPay IPN: invalid signature, params={}", params);
            return ipnResponse("97", "Invalid signature");
        }

        String vnpTxnRef = params.get("vnp_TxnRef");
        String vnpAmountStr = params.get("vnp_Amount");

        Transaction txn = transactionRepository.findByVnpTxnRef(vnpTxnRef).orElse(null);
        if (txn == null) {
            log.warn("VNPay IPN: transaction not found, txnRef={}", vnpTxnRef);
            return ipnResponse("01", "Order not found");
        }

        if (vnpAmountStr != null) {
            try {
                long vnpAmount = Long.parseLong(vnpAmountStr);
                long expectedAmount = txn.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
                if (vnpAmount != expectedAmount) {
                    log.warn("VNPay IPN: amount mismatch, expected={}, received={}", expectedAmount, vnpAmount);
                    return ipnResponse("04", "Invalid amount");
                }
            } catch (NumberFormatException e) {
                log.warn("VNPay IPN: cannot parse amount={}", vnpAmountStr);
                return ipnResponse("04", "Invalid amount");
            }
        }

        if (txn.getStatus() == TransactionStatus.SUCCESS) {
            log.info("VNPay IPN: already confirmed, txnRef={}", vnpTxnRef);
            return ipnResponse("02", "Order already confirmed");
        }

        processPaymentResult(params, txn, "IPN");
        return ipnResponse("00", "Confirm Success");
    }

    @Override
    @Transactional
    public String buildReturnRedirect(Map<String, String> params) {
        if (!VNPayUtil.verifySignature(params, vnPayConfig.getHashSecret())) {
            log.warn("VNPay Return: invalid signature");
            return vnPayConfig.getFrontendCancelUrl() + "?reason=invalid_signature";
        }

        String vnpResponseCode = params.get("vnp_ResponseCode");
        String vnpTxnRef = params.get("vnp_TxnRef");

        // Cập nhật state idempotently — phòng trường hợp IPN không đến được (môi trường local/dev)
        Transaction txn = transactionRepository.findByVnpTxnRef(vnpTxnRef).orElse(null);
        if (txn != null && txn.getStatus() == TransactionStatus.PENDING) {
            processPaymentResult(params, txn, "Return");
        }

        if ("00".equals(vnpResponseCode)) {
            return vnPayConfig.getFrontendSuccessUrl() + "?txnRef=" + vnpTxnRef;
        } else {
            return vnPayConfig.getFrontendCancelUrl() + "?code=" + vnpResponseCode;
        }
    }

    /**
     * Xử lý kết quả thanh toán (dùng chung cho IPN và Return URL).
     * Caller phải đảm bảo idempotent check trước khi gọi.
     */
    private void processPaymentResult(Map<String, String> params, Transaction txn, String source) {
        String vnpResponseCode      = params.get("vnp_ResponseCode");
        String vnpTransactionStatus = params.get("vnp_TransactionStatus");
        String vnpTransactionNo     = params.get("vnp_TransactionNo");
        String vnpBankCode          = params.get("vnp_BankCode");
        String vnpTxnRef            = params.get("vnp_TxnRef");
        String vnpResponseJson      = toJson(params);

        if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
            txn.setStatus(TransactionStatus.SUCCESS);
            txn.setVnpTransactionNo(vnpTransactionNo);
            txn.setVnpBankCode(vnpBankCode);
            txn.setVnpResponseCode(vnpResponseCode);
            txn.setVnpResponse(vnpResponseJson);
            txn.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(txn);

            Contract contract = contractRepository.findByIdAndDeletedAtIsNull(txn.getContractId()).orElse(null);
            if (contract != null && contract.getStatus() == ContractStatus.PENDING_PAYMENT) {
                String oldStatus = contract.getStatus().name();
                contract.setStatus(ContractStatus.IN_PROGRESS);
                contractRepository.save(contract);

                try {
                    auditLogService.log(
                            txn.getUserId(),
                            "CONTRACT_ACTIVATED",
                            "CONTRACT",
                            contract.getId(),
                            "{\"status\":\"" + oldStatus + "\"}",
                            "{\"status\":\"IN_PROGRESS\",\"txnRef\":\"" + vnpTxnRef + "\",\"source\":\"" + source + "\"}",
                            null
                    );
                } catch (Exception auditEx) {
                    log.warn("Audit log failed for CONTRACT_ACTIVATED contractId={}: {}", contract.getId(), auditEx.getMessage());
                }
                log.info("Contract {} activated via VNPay {} txnRef={}", contract.getId(), source, vnpTxnRef);

                final String jobTitle = jobRepository.findById(contract.getJobId())
                        .map(j -> j.getTitle()).orElse("(job)");
                eventPublisher.publish("notification.user", PaymentReceivedEvent.builder()
                        .recipientUserId(contract.getFreelancerId())
                        .referenceType("Contract")
                        .referenceId(contract.getId())
                        .jobTitle(jobTitle)
                        .amount(contract.getAgreedPrice())
                        .occurredAt(LocalDateTime.now())
                        .build());
            }
        } else {
            txn.setStatus(TransactionStatus.FAILED);
            txn.setVnpResponseCode(vnpResponseCode);
            txn.setVnpResponse(vnpResponseJson);
            txn.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(txn);
            log.info("VNPay {}: payment failed, txnRef={}, code={}", source, vnpTxnRef, vnpResponseCode);
        }
    }

    // ───────── helpers ─────────

    private Map<String, String> ipnResponse(String code, String message) {
        Map<String, String> resp = new HashMap<>();
        resp.put("RspCode", code);
        resp.put("Message", message);
        return resp;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
