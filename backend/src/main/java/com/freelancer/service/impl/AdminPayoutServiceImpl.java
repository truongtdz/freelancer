package com.freelancer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelancer.dto.request.PayoutCreateRequest;
import com.freelancer.dto.response.PayoutResponse;
import com.freelancer.dto.response.PendingPayoutItem;
import com.freelancer.dto.response.UserSummaryResponse;
import com.freelancer.entity.*;
import com.freelancer.entity.enums.*;
import com.freelancer.event.EventPublisher;
import com.freelancer.event.PayoutCompletedEvent;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.repository.*;
import com.freelancer.service.AdminPayoutService;
import com.freelancer.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPayoutServiceImpl implements AdminPayoutService {

    private final ContractRepository contractRepository;
    private final PayoutRepository payoutRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CompletionSubmissionRepository submissionRepository;
    private final PaymentInfoRepository paymentInfoRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;

    @Override
    public Page<PendingPayoutItem> getPendingPayouts(Pageable pageable) {
        Page<Contract> page = contractRepository.findByStatus(ContractStatus.CLIENT_CONFIRMED, pageable);

        List<PendingPayoutItem> items = page.getContent().stream()
                .filter(c -> payoutRepository.findByContractId(c.getId()).isEmpty())
                .map(this::buildPendingPayoutItem)
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public PayoutResponse payout(Long contractId, PayoutCreateRequest req, Long adminId) {
        // 1. Load contract
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        // 2. Validate status
        if (c.getStatus() != ContractStatus.CLIENT_CONFIRMED) {
            throw new AppException(ErrorCode.CONTRACT_NOT_READY_PAYOUT);
        }

        // 3. Check not already paid
        if (payoutRepository.findByContractId(contractId).isPresent()) {
            throw new AppException(ErrorCode.PAYOUT_ALREADY_EXISTS);
        }

        // 4. Find confirmed submission
        CompletionSubmission lastSubmission = submissionRepository
                .findTopByContractIdAndStatusOrderByAttemptNumberDesc(contractId, SubmissionStatus.CONFIRMED)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR,
                        "No confirmed submission found for contract " + contractId));

        // 5. Build bank info snapshot
        String bankSnapshot = buildBankSnapshot(c, lastSubmission);

        BigDecimal gross = nvl(c.getAgreedPrice());
        BigDecimal commission = nvl(c.getCommissionAmount());
        BigDecimal net = nvl(c.getNetAmount());

        // 6. Create Payout
        Payout payout = payoutRepository.save(Payout.builder()
                .payoutCode("PAY-" + System.currentTimeMillis())
                .contractId(contractId)
                .freelancerId(c.getFreelancerId())
                .adminId(adminId)
                .grossAmount(gross)
                .commissionAmount(commission)
                .netAmount(net)
                .qrCodeUrl(lastSubmission.getQrCodeUrl())
                .bankInfoSnapshot(bankSnapshot)
                .proofImageUrl(req.getProofImageUrl())
                .status(PayoutStatus.COMPLETED)
                .paidAt(LocalDateTime.now())
                .build());

        // 7. Create 2 transactions
        String txBase = "TXN-PAY-" + payout.getId() + "-" + System.currentTimeMillis();
        transactionRepository.save(Transaction.builder()
                .transactionCode(txBase + "-N")
                .contractId(contractId)
                .userId(c.getFreelancerId())
                .type(TransactionType.PAYOUT)
                .amount(net)
                .status(TransactionStatus.SUCCESS)
                .processedByAdminId(adminId)
                .note(req.getNote())
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build());
        transactionRepository.save(Transaction.builder()
                .transactionCode(txBase + "-C")
                .contractId(contractId)
                .userId(adminId)
                .type(TransactionType.COMMISSION)
                .amount(commission)
                .status(TransactionStatus.SUCCESS)
                .processedByAdminId(adminId)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build());

        // 8. Update contract to PAID_OUT
        c.setStatus(ContractStatus.PAID_OUT);
        contractRepository.save(c);

        // 9. Increment freelancer's total_jobs_done
        userProfileRepository.incrementTotalJobsDone(c.getFreelancerId());

        // 10. Audit
        try {
            auditLogService.log(adminId, "PAYOUT", "Contract", c.getId(),
                    null, net.toPlainString(), null);
        } catch (Exception auditEx) {
            log.warn("Audit log failed for PAYOUT contractId={}: {}", contractId, auditEx.getMessage());
        }

        // 11. Publish notification to freelancer
        eventPublisher.publish("notification.user", PayoutCompletedEvent.builder()
                .recipientUserId(c.getFreelancerId())
                .referenceType("Payout")
                .referenceId(payout.getId())
                .netAmount(net)
                .occurredAt(LocalDateTime.now())
                .build());

        // Build response
        User freelancer = userRepository.findById(c.getFreelancerId()).orElse(null);
        User admin = userRepository.findById(adminId).orElse(null);
        return toPayoutResponse(payout, c, freelancer, admin);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private PendingPayoutItem buildPendingPayoutItem(Contract c) {
        User freelancer = userRepository.findById(c.getFreelancerId()).orElse(null);
        User client = userRepository.findById(c.getClientId()).orElse(null);

        String qrCodeUrl = submissionRepository
                .findTopByContractIdAndStatusOrderByAttemptNumberDesc(c.getId(), SubmissionStatus.CONFIRMED)
                .map(CompletionSubmission::getQrCodeUrl)
                .orElse(null);

        String bankSnapshot = buildBankSnapshot(c, null);

        return PendingPayoutItem.builder()
                .contractId(c.getId())
                .contractCode(c.getContractCode())
                .freelancer(toSummary(freelancer))
                .client(toSummary(client))
                .agreedPrice(nvl(c.getAgreedPrice()))
                .commissionAmount(nvl(c.getCommissionAmount()))
                .netAmount(nvl(c.getNetAmount()))
                .qrCodeUrl(qrCodeUrl)
                .bankInfoSnapshot(bankSnapshot)
                .confirmedAt(c.getUpdatedAt())
                .build();
    }

    private String buildBankSnapshot(Contract c, CompletionSubmission submission) {
        PaymentInfo pi = null;
        // Try submission's payment info first
        if (submission != null && submission.getPaymentInfoId() != null) {
            pi = paymentInfoRepository.findById(submission.getPaymentInfoId()).orElse(null);
        }
        // Fallback to default payment info
        if (pi == null) {
            pi = paymentInfoRepository.findByUserIdAndIsDefaultTrue(c.getFreelancerId()).orElse(null);
        }
        if (pi == null) return "{}";
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "bankName", pi.getBankName(),
                    "accountNumber", pi.getBankAccountNumber(),
                    "accountHolder", pi.getBankAccountHolder()
            ));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize bank info snapshot", e);
            return "{}";
        }
    }

    private PayoutResponse toPayoutResponse(Payout p, Contract c, User freelancer, User admin) {
        return PayoutResponse.builder()
                .id(p.getId())
                .payoutCode(p.getPayoutCode())
                .contractId(c.getId())
                .contractCode(c.getContractCode())
                .freelancer(toSummary(freelancer))
                .admin(toSummary(admin))
                .grossAmount(p.getGrossAmount())
                .commissionAmount(p.getCommissionAmount())
                .netAmount(p.getNetAmount())
                .qrCodeUrl(p.getQrCodeUrl())
                .bankInfoSnapshot(p.getBankInfoSnapshot())
                .proofImageUrl(p.getProofImageUrl())
                .status(p.getStatus())
                .paidAt(p.getPaidAt())
                .build();
    }

    private UserSummaryResponse toSummary(User u) {
        if (u == null) return null;
        return UserSummaryResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .avatarUrl(u.getAvatarUrl())
                .role(u.getRole())
                .status(u.getStatus())
                .build();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
