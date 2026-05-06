package com.freelancer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelancer.dto.request.DisputeResolveRequest;
import com.freelancer.dto.response.DisputeResponse;
import com.freelancer.dto.response.UserSummaryResponse;
import com.freelancer.entity.*;
import com.freelancer.entity.enums.*;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.repository.*;
import com.freelancer.service.AdminDisputeService;
import com.freelancer.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDisputeServiceImpl implements AdminDisputeService {

    private final DisputeRepository disputeRepository;
    private final ContractRepository contractRepository;
    private final PayoutRepository payoutRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CompletionSubmissionRepository submissionRepository;
    private final PaymentInfoRepository paymentInfoRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Override
    public Page<DisputeResponse> getDisputes(DisputeStatus status, Pageable pageable) {
        Page<Dispute> page = (status != null)
                ? disputeRepository.findByStatus(status, pageable)
                : disputeRepository.findAll(pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public DisputeResponse resolveDispute(Long disputeId, DisputeResolveRequest req, Long adminId) {
        // 1. Load dispute
        Dispute d = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));

        // 2. Validate status
        if (d.getStatus() != DisputeStatus.OPEN && d.getStatus() != DisputeStatus.IN_REVIEW) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Tranh chấp đã được giải quyết hoặc đóng");
        }

        // 3. Load contract
        Contract c = contractRepository.findById(d.getContractId())
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        BigDecimal agreedPrice = nvl(c.getAgreedPrice());
        BigDecimal commissionRate = nvl(c.getCommissionRate());

        // 4. Process resolution
        switch (req.getResolutionType()) {
            case FULL_REFUND -> {
                // Refund full amount to client, cancel contract
                createRefundTransaction(c, agreedPrice, adminId);
                c.setStatus(ContractStatus.CANCELLED);
            }
            case FULL_PAYOUT -> {
                // Full payout to freelancer
                doFullPayout(c, adminId, null, null);
                c.setStatus(ContractStatus.PAID_OUT);
                userProfileRepository.incrementTotalJobsDone(c.getFreelancerId());
            }
            case PARTIAL -> {
                BigDecimal partial = req.getPartialAmountToFreelancer();
                if (partial == null || partial.compareTo(BigDecimal.ZERO) <= 0
                        || partial.compareTo(agreedPrice) >= 0) {
                    throw new AppException(ErrorCode.INVALID_PARTIAL_AMOUNT);
                }
                // commission on partial amount
                BigDecimal commission = partial.multiply(commissionRate)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal net = partial.subtract(commission);
                BigDecimal refund = agreedPrice.subtract(partial);

                doPartialPayout(c, partial, commission, net, adminId);
                createRefundTransaction(c, refund, adminId);
                c.setStatus(ContractStatus.PAID_OUT);
                userProfileRepository.incrementTotalJobsDone(c.getFreelancerId());
            }
        }
        contractRepository.save(c);

        // 5. Resolve dispute
        d.setStatus(DisputeStatus.RESOLVED);
        d.setResolution(req.getResolution());
        d.setResolvedByAdminId(adminId);
        d.setResolvedAt(LocalDateTime.now());
        d = disputeRepository.save(d);

        // 6. Audit
        try {
            auditLogService.log(adminId, "DISPUTE_RESOLVE", "Dispute", d.getId(),
                    null, "\"" + req.getResolutionType().name() + "\"", null);
        } catch (Exception auditEx) {
            log.warn("Audit log failed for DISPUTE_RESOLVE disputeId={}: {}", d.getId(), auditEx.getMessage());
        }

        return toResponse(d);
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private void doFullPayout(Contract c, Long adminId, String proofImageUrl, String note) {
        BigDecimal gross = nvl(c.getAgreedPrice());
        BigDecimal commission = nvl(c.getCommissionAmount());
        BigDecimal net = nvl(c.getNetAmount());

        String bankSnapshot = buildBankSnapshot(c);

        Payout payout = payoutRepository.save(Payout.builder()
                .payoutCode("PAY-D-" + System.currentTimeMillis())
                .contractId(c.getId())
                .freelancerId(c.getFreelancerId())
                .adminId(adminId)
                .grossAmount(gross)
                .commissionAmount(commission)
                .netAmount(net)
                .bankInfoSnapshot(bankSnapshot)
                .proofImageUrl(proofImageUrl)
                .status(PayoutStatus.COMPLETED)
                .paidAt(LocalDateTime.now())
                .build());

        String txBase = "TXN-DP-" + payout.getId() + "-" + System.currentTimeMillis();
        saveTransaction(txBase + "-N", c.getId(), c.getFreelancerId(),
                TransactionType.PAYOUT, net, adminId, note);
        saveTransaction(txBase + "-C", c.getId(), adminId,
                TransactionType.COMMISSION, commission, adminId, null);
    }

    private void doPartialPayout(Contract c, BigDecimal gross, BigDecimal commission,
                                  BigDecimal net, Long adminId) {
        String bankSnapshot = buildBankSnapshot(c);

        Payout payout = payoutRepository.save(Payout.builder()
                .payoutCode("PAY-P-" + System.currentTimeMillis())
                .contractId(c.getId())
                .freelancerId(c.getFreelancerId())
                .adminId(adminId)
                .grossAmount(gross)
                .commissionAmount(commission)
                .netAmount(net)
                .bankInfoSnapshot(bankSnapshot)
                .status(PayoutStatus.COMPLETED)
                .paidAt(LocalDateTime.now())
                .build());

        String txBase = "TXN-PP-" + payout.getId() + "-" + System.currentTimeMillis();
        saveTransaction(txBase + "-N", c.getId(), c.getFreelancerId(),
                TransactionType.PAYOUT, net, adminId, null);
        saveTransaction(txBase + "-C", c.getId(), adminId,
                TransactionType.COMMISSION, commission, adminId, null);
    }

    private void createRefundTransaction(Contract c, BigDecimal amount, Long adminId) {
        String code = "TXN-REF-" + c.getId() + "-" + System.currentTimeMillis();
        saveTransaction(code, c.getId(), c.getClientId(),
                TransactionType.REFUND, amount, adminId, null);
    }

    private void saveTransaction(String code, Long contractId, Long userId,
                                  TransactionType type, BigDecimal amount,
                                  Long adminId, String note) {
        transactionRepository.save(Transaction.builder()
                .transactionCode(code)
                .contractId(contractId)
                .userId(userId)
                .type(type)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .processedByAdminId(adminId)
                .note(note)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build());
    }

    private String buildBankSnapshot(Contract c) {
        PaymentInfo pi = paymentInfoRepository.findByUserIdAndIsDefaultTrue(c.getFreelancerId())
                .orElse(null);
        if (pi == null) return "{}";
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "bankName", pi.getBankName(),
                    "accountNumber", pi.getBankAccountNumber(),
                    "accountHolder", pi.getBankAccountHolder()
            ));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private DisputeResponse toResponse(Dispute d) {
        return DisputeResponse.builder()
                .id(d.getId())
                .reason(d.getReason())
                .description(d.getDescription())
                .status(d.getStatus())
                .resolution(d.getResolution())
                .createdAt(d.getCreatedAt())
                .resolvedAt(d.getResolvedAt())
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
