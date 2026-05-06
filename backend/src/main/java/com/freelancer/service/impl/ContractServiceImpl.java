package com.freelancer.service.impl;

import com.freelancer.dto.request.CompletionRejectRequest;
import com.freelancer.dto.request.CompletionSubmitRequest;
import com.freelancer.dto.request.DisputeRequest;
import com.freelancer.dto.request.ProgressReportRequest;
import com.freelancer.dto.response.*;
import com.freelancer.entity.*;
import com.freelancer.entity.enums.*;
import com.freelancer.event.*;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.mapper.UserMapper;
import com.freelancer.payment.VNPayService;
import com.freelancer.repository.*;
import com.freelancer.service.AuditLogService;
import com.freelancer.service.ContractService;
import com.freelancer.service.SystemSettingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final ContractRepository             contractRepository;
    private final UserRepository                 userRepository;
    private final JobRepository                  jobRepository;
    private final ProgressReportRepository       progressReportRepository;
    private final ProgressAttachmentRepository   progressAttachmentRepository;
    private final CompletionSubmissionRepository  submissionRepository;
    private final TransactionRepository          transactionRepository;
    private final PayoutRepository               payoutRepository;
    private final DisputeRepository              disputeRepository;
    private final VNPayService                   vnPayService;
    private final SystemSettingService           systemSettingService;
    private final AuditLogService                auditLogService;
    private final UserMapper                     userMapper;
    private final EventPublisher                 eventPublisher;

    // ─────────────────────────────────────────────────────────────
    // List
    // ─────────────────────────────────────────────────────────────

    @Override
    public Page<ContractListItemResponse> getMyContracts(Long userId, String role,
                                                          ContractStatus statusFilter,
                                                          Pageable pageable) {
        boolean isClient = "CLIENT".equals(role);

        Page<Contract> page;
        if (isClient) {
            page = statusFilter != null
                    ? contractRepository.findByClientIdAndStatusAndDeletedAtIsNull(userId, statusFilter, pageable)
                    : contractRepository.findByClientIdAndDeletedAtIsNull(userId, pageable);
        } else {
            page = statusFilter != null
                    ? contractRepository.findByFreelancerIdAndStatusAndDeletedAtIsNull(userId, statusFilter, pageable)
                    : contractRepository.findByFreelancerIdAndDeletedAtIsNull(userId, pageable);
        }

        return page.map(c -> toListItem(c, userId, isClient));
    }

    // ─────────────────────────────────────────────────────────────
    // Detail
    // ─────────────────────────────────────────────────────────────

    @Override
    public ContractDetailResponse getContractById(Long contractId, Long userId) {
        Contract c = loadContract(contractId);
        assertParticipant(c, userId);
        return buildDetail(c, userId);
    }

    // ─────────────────────────────────────────────────────────────
    // Payment
    // ─────────────────────────────────────────────────────────────

    @Override
    public String initiatePayment(Long contractId, Long clientId, HttpServletRequest request) {
        return vnPayService.createPaymentUrl(contractId, clientId, request);
    }

    // ─────────────────────────────────────────────────────────────
    // Progress Report
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProgressReportResponse createProgressReport(Long contractId,
                                                        ProgressReportRequest req,
                                                        Long freelancerId) {
        Contract c = loadContract(contractId);

        if (!Objects.equals(c.getFreelancerId(), freelancerId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Chỉ freelancer của hợp đồng mới được báo cáo");
        }
        if (c.getStatus() != ContractStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Chỉ có thể báo cáo tiến độ khi hợp đồng đang IN_PROGRESS");
        }

        ProgressReport report = ProgressReport.builder()
                .contractId(contractId)
                .freelancerId(freelancerId)
                .title(req.getTitle())
                .content(req.getContent())
                .progressPercentage(req.getProgressPercentage())
                .reportedAt(LocalDateTime.now())
                .build();
        report = progressReportRepository.save(report);

        // Lưu attachments vào bảng progress_attachments
        List<String> savedUrls = List.of();
        if (req.getAttachmentUrls() != null && !req.getAttachmentUrls().isEmpty()) {
            final Long reportId = report.getId();
            List<ProgressAttachment> attachments = req.getAttachmentUrls().stream()
                    .map(url -> ProgressAttachment.builder()
                            .progressReportId(reportId)
                            .fileUrl(url)
                            .uploadedAt(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());
            progressAttachmentRepository.saveAll(attachments);
            savedUrls = req.getAttachmentUrls();
        }

        // Publish notification to client
        eventPublisher.publish("notification.user", ProgressReportEvent.builder()
                .recipientUserId(c.getClientId())
                .referenceType("Contract")
                .referenceId(contractId)
                .progressPercentage(req.getProgressPercentage())
                .occurredAt(LocalDateTime.now())
                .build());

        return toProgressReportResponse(report, savedUrls);
    }

    // ─────────────────────────────────────────────────────────────
    // Submit Completion
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ContractDetailResponse submitCompletion(Long contractId,
                                                    CompletionSubmitRequest req,
                                                    Long freelancerId) {
        Contract c = loadContract(contractId);

        if (!Objects.equals(c.getFreelancerId(), freelancerId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Chỉ freelancer của hợp đồng mới được nộp bàn giao");
        }

        // Validate status
        boolean statusOk = c.getStatus() == ContractStatus.IN_PROGRESS
                || (c.getStatus() == ContractStatus.FREELANCER_SUBMITTED
                    && submissionRepository.findTopByContractIdOrderByAttemptNumberDesc(contractId)
                       .map(s -> s.getStatus() == SubmissionStatus.REJECTED).orElse(false));

        if (!statusOk) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Không thể nộp bàn giao ở trạng thái hiện tại");
        }

        int maxAttempts = systemSettingService.getInt("MAX_SUBMISSION_ATTEMPTS", DEFAULT_MAX_ATTEMPTS);
        long attempts = submissionRepository.countByContractId(contractId);

        if (attempts >= maxAttempts) {
            throw new AppException(ErrorCode.SUBMISSION_LIMIT_EXCEEDED);
        }

        // Mark old REJECTED submissions as SUPERSEDED
        submissionRepository.supersedeRejected(contractId);

        // Build deliverables CSV
        String deliverablesCsv = String.join(",", req.getDeliverableUrls());

        CompletionSubmission submission = CompletionSubmission.builder()
                .contractId(contractId)
                .freelancerId(freelancerId)
                .summary(req.getSummary())
                .deliverablesUrl(deliverablesCsv)
                .paymentInfoId(req.getPaymentInfoId())
                .qrCodeUrl(req.getQrCodeUrl())
                .status(SubmissionStatus.PENDING_CONFIRM)
                .attemptNumber((int) attempts + 1)
                .submittedAt(LocalDateTime.now())
                .build();
        submissionRepository.save(submission);

        c.setStatus(ContractStatus.FREELANCER_SUBMITTED);
        contractRepository.save(c);

        try {
            auditLogService.log(freelancerId, "COMPLETION_SUBMITTED", "CONTRACT", contractId,
                    "{\"attempt\":" + submission.getAttemptNumber() + "}", null, null);
        } catch (Exception auditEx) {
            log.warn("Audit log failed for COMPLETION_SUBMITTED contractId={}: {}", contractId, auditEx.getMessage());
        }

        // Publish notification to client
        eventPublisher.publish("notification.user", CompletionSubmittedEvent.builder()
                .recipientUserId(c.getClientId())
                .referenceType("Contract")
                .referenceId(contractId)
                .contractCode(c.getContractCode())
                .occurredAt(LocalDateTime.now())
                .build());

        return buildDetail(c, freelancerId);
    }

    // ─────────────────────────────────────────────────────────────
    // Confirm Completion
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ContractDetailResponse confirmCompletion(Long contractId, Long clientId, boolean auto) {
        Contract c = loadContract(contractId);

        if (!auto && !Objects.equals(c.getClientId(), clientId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Chỉ client của hợp đồng mới được xác nhận");
        }
        if (c.getStatus() != ContractStatus.FREELANCER_SUBMITTED) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Hợp đồng không ở trạng thái FREELANCER_SUBMITTED");
        }

        submissionRepository.findTopByContractIdOrderByAttemptNumberDesc(contractId).ifPresent(s -> {
            s.setStatus(SubmissionStatus.CONFIRMED);
            s.setClientConfirmedAt(LocalDateTime.now());
            submissionRepository.save(s);
        });

        c.setStatus(ContractStatus.CLIENT_CONFIRMED);
        contractRepository.save(c);

        String action = auto ? "AUTO_CONFIRM_COMPLETION" : "CONFIRM_COMPLETION";
        auditLogService.log(auto ? null : clientId, action, "CONTRACT", contractId, null);

        // Publish notification to freelancer
        eventPublisher.publish("notification.user", CompletionConfirmedEvent.builder()
                .recipientUserId(c.getFreelancerId())
                .referenceType("Contract")
                .referenceId(contractId)
                .contractCode(c.getContractCode())
                .occurredAt(LocalDateTime.now())
                .build());

        return buildDetail(c, auto ? c.getClientId() : clientId);
    }

    // ─────────────────────────────────────────────────────────────
    // Reject Completion
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ContractDetailResponse rejectCompletion(Long contractId,
                                                    CompletionRejectRequest req,
                                                    Long clientId) {
        Contract c = loadContract(contractId);

        if (!Objects.equals(c.getClientId(), clientId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Chỉ client của hợp đồng mới được từ chối");
        }
        if (c.getStatus() != ContractStatus.FREELANCER_SUBMITTED) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Hợp đồng không ở trạng thái FREELANCER_SUBMITTED");
        }

        CompletionSubmission last = submissionRepository
                .findTopByContractIdOrderByAttemptNumberDesc(contractId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy bàn giao"));

        last.setStatus(SubmissionStatus.REJECTED);
        last.setRejectionReason(req.getReason());
        submissionRepository.save(last);

        c.setStatus(ContractStatus.IN_PROGRESS);
        contractRepository.save(c);

        int maxAttempts = systemSettingService.getInt("MAX_SUBMISSION_ATTEMPTS", DEFAULT_MAX_ATTEMPTS);
        long totalAttempts = submissionRepository.countByContractId(contractId);
        if (totalAttempts >= maxAttempts) {
            log.warn("Contract {} đã đạt giới hạn {} submit. Đề nghị mở dispute.", contractId, maxAttempts);
        }

        try {
            auditLogService.log(clientId, "COMPLETION_REJECTED", "CONTRACT", contractId,
                    "{\"reason\":\"" + req.getReason().replace("\"", "'") + "\"}", null, null);
        } catch (Exception auditEx) {
            log.warn("Audit log failed for COMPLETION_REJECTED contractId={}: {}", contractId, auditEx.getMessage());
        }

        return buildDetail(c, clientId);
    }

    // ─────────────────────────────────────────────────────────────
    // Dispute
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ContractDetailResponse raiseDispute(Long contractId, DisputeRequest req, Long userId) {
        Contract c = loadContract(contractId);
        assertParticipant(c, userId);

        if (c.getStatus() != ContractStatus.IN_PROGRESS
                && c.getStatus() != ContractStatus.FREELANCER_SUBMITTED) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Chỉ có thể mở tranh chấp khi hợp đồng IN_PROGRESS hoặc FREELANCER_SUBMITTED");
        }
        if (disputeRepository.existsByContractId(contractId)) {
            throw new AppException(ErrorCode.DISPUTE_ALREADY_EXISTS);
        }

        Dispute dispute = Dispute.builder()
                .contractId(contractId)
                .raisedByUserId(userId)
                .reason(req.getReason())
                .description(req.getDescription())
                .status(DisputeStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
        disputeRepository.save(dispute);

        c.setStatus(ContractStatus.DISPUTED);
        contractRepository.save(c);

        auditLogService.log(userId, "DISPUTE_RAISED", "CONTRACT", contractId, null);

        return buildDetail(c, userId);
    }

    // ─────────────────────────────────────────────────────────────
    // Scheduler actions
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void autoCancelExpiredContracts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Contract> expired = contractRepository.findExpiredPendingPayment(cutoff);
        for (Contract c : expired) {
            log.info("Auto-cancelling contract {} (PENDING_PAYMENT > 24h)", c.getId());
            c.setStatus(ContractStatus.CANCELLED);
            c.setDeletedAt(LocalDateTime.now());
            contractRepository.save(c);

            jobRepository.findByIdAndDeletedAtIsNull(c.getJobId()).ifPresent(job -> {
                if (job.getStatus() == JobStatus.IN_PROGRESS) {
                    job.setStatus(JobStatus.OPEN);
                    jobRepository.save(job);
                }
            });

            auditLogService.log(null, "CONTRACT_AUTO_CANCELLED", "CONTRACT", c.getId(), null);
        }
        if (!expired.isEmpty()) log.info("Auto-cancelled {} contracts", expired.size());
    }

    @Override
    @Transactional
    public void autoConfirmCompletion() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<Contract> contracts = contractRepository.findAutoConfirmable(cutoff);
        for (Contract c : contracts) {
            log.info("Auto-confirming contract {} (FREELANCER_SUBMITTED > 7 days)", c.getId());
            confirmCompletion(c.getId(), null, true);
        }
        if (!contracts.isEmpty()) log.info("Auto-confirmed {} contracts", contracts.size());
    }

    // ─────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────

    private Contract loadContract(Long contractId) {
        return contractRepository.findByIdAndDeletedAtIsNull(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
    }

    private void assertParticipant(Contract c, Long userId) {
        if (!Objects.equals(c.getClientId(), userId) && !Objects.equals(c.getFreelancerId(), userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không phải bên trong hợp đồng");
        }
    }

    private ContractListItemResponse toListItem(Contract c, Long userId, boolean isClient) {
        String jobTitle = jobRepository.findByIdAndDeletedAtIsNull(c.getJobId())
                .map(Job::getTitle).orElse("(Job đã xoá)");

        Long counterpartyId = isClient ? c.getFreelancerId() : c.getClientId();
        User counterparty = userRepository.findById(counterpartyId).orElse(null);

        return ContractListItemResponse.builder()
                .id(c.getId())
                .contractCode(c.getContractCode())
                .jobId(c.getJobId())
                .jobTitle(jobTitle)
                .counterparty(counterparty != null ? userMapper.toSummary(counterparty) : null)
                .agreedPrice(c.getAgreedPrice())
                .status(c.getStatus())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ContractDetailResponse buildDetail(Contract c, Long viewerUserId) {
        // Load related parties
        User clientUser     = userRepository.findById(c.getClientId()).orElse(null);
        User freelancerUser = userRepository.findById(c.getFreelancerId()).orElse(null);
        String jobTitle     = jobRepository.findByIdAndDeletedAtIsNull(c.getJobId())
                .map(Job::getTitle).orElse("(Job đã xoá)");

        // Load related collections
        List<ProgressReport> reports = progressReportRepository
                .findByContractIdOrderByReportedAtDesc(c.getId());

        // Batch-load attachments to avoid N+1
        List<Long> reportIds = reports.stream().map(ProgressReport::getId).toList();
        java.util.Map<Long, List<String>> attachmentsByReportId = reportIds.isEmpty()
                ? java.util.Map.of()
                : progressAttachmentRepository.findByProgressReportIdIn(reportIds).stream()
                    .collect(Collectors.groupingBy(
                            ProgressAttachment::getProgressReportId,
                            Collectors.mapping(ProgressAttachment::getFileUrl, Collectors.toList())
                    ));
        List<CompletionSubmission> submissions = submissionRepository
                .findByContractIdOrderByAttemptNumberDesc(c.getId());
        List<Transaction> transactions = transactionRepository
                .findByContractIdOrderByCreatedAtDesc(c.getId());
        Payout payout     = payoutRepository.findByContractId(c.getId()).orElse(null);
        Dispute dispute   = disputeRepository.findByContractId(c.getId()).orElse(null);

        int maxAttempts  = systemSettingService.getInt("MAX_SUBMISSION_ATTEMPTS", DEFAULT_MAX_ATTEMPTS);
        long attemptsDone = submissions.size();

        // Compute permission flags
        boolean isClient     = Objects.equals(c.getClientId(), viewerUserId);
        boolean isFreelancer = Objects.equals(c.getFreelancerId(), viewerUserId);

        boolean lastSubmitRejected = submissions.stream().findFirst()
                .map(s -> s.getStatus() == SubmissionStatus.REJECTED).orElse(false);

        boolean canSubmitCompletion = isFreelancer
                && (c.getStatus() == ContractStatus.IN_PROGRESS
                    || (c.getStatus() == ContractStatus.FREELANCER_SUBMITTED && lastSubmitRejected))
                && attemptsDone < maxAttempts;

        boolean canRejectCompletion = isClient
                && c.getStatus() == ContractStatus.FREELANCER_SUBMITTED
                && attemptsDone < maxAttempts;

        return ContractDetailResponse.builder()
                .id(c.getId())
                .contractCode(c.getContractCode())
                .jobId(c.getJobId())
                .jobTitle(jobTitle)
                .client(clientUser != null ? userMapper.toSummary(clientUser) : null)
                .freelancer(freelancerUser != null ? userMapper.toSummary(freelancerUser) : null)
                .agreedPrice(c.getAgreedPrice())
                .commissionRate(c.getCommissionRate())
                .commissionAmount(c.getCommissionAmount())
                .netAmount(c.getNetAmount())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                // related
                .progressReports(reports.stream()
                        .map(r -> toProgressReportResponse(r, attachmentsByReportId.get(r.getId())))
                        .toList())
                .completionSubmissions(submissions.stream().map(this::toSubmissionResponse).toList())
                .transactions(transactions.stream().map(this::toTransactionSummary).toList())
                .payout(payout != null ? toPayoutResponse(payout) : null)
                .dispute(dispute != null ? toDisputeResponse(dispute) : null)
                // flags
                .canPay(isClient && c.getStatus() == ContractStatus.PENDING_PAYMENT)
                .canReportProgress(isFreelancer && c.getStatus() == ContractStatus.IN_PROGRESS)
                .canSubmitCompletion(canSubmitCompletion)
                .canConfirmCompletion(isClient && c.getStatus() == ContractStatus.FREELANCER_SUBMITTED)
                .canRejectCompletion(canRejectCompletion)
                .canRaiseDispute((isClient || isFreelancer)
                        && (c.getStatus() == ContractStatus.IN_PROGRESS
                            || c.getStatus() == ContractStatus.FREELANCER_SUBMITTED)
                        && dispute == null)
                .submissionAttempts((int) attemptsDone)
                .maxSubmissionAttempts(maxAttempts)
                .build();
    }

    // ─── Tiny mappers ───

    private ProgressReportResponse toProgressReportResponse(ProgressReport r, List<String> attachmentUrls) {
        return ProgressReportResponse.builder()
                .id(r.getId())
                .contractId(r.getContractId())
                .title(r.getTitle())
                .content(r.getContent())
                .progressPercentage(r.getProgressPercentage())
                .attachmentUrls(attachmentUrls != null ? attachmentUrls : List.of())
                .clientFeedback(r.getClientFeedback())
                .createdAt(r.getReportedAt())
                .build();
    }

    private CompletionSubmissionResponse toSubmissionResponse(CompletionSubmission s) {
        List<String> urls = (s.getDeliverablesUrl() != null && !s.getDeliverablesUrl().isEmpty())
                ? Arrays.asList(s.getDeliverablesUrl().split(","))
                : List.of();
        return CompletionSubmissionResponse.builder()
                .id(s.getId())
                .contractId(s.getContractId())
                .summary(s.getSummary())
                .deliverableUrls(urls)
                .qrCodeUrl(s.getQrCodeUrl())
                .status(s.getStatus())
                .rejectionReason(s.getRejectionReason())
                .attemptNumber(s.getAttemptNumber())
                .createdAt(s.getSubmittedAt())
                .build();
    }

    private TransactionSummary toTransactionSummary(Transaction t) {
        return TransactionSummary.builder()
                .id(t.getId())
                .transactionCode(t.getTransactionCode())
                .type(t.getType())
                .amount(t.getAmount())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private PayoutResponse toPayoutResponse(Payout p) {
        return PayoutResponse.builder()
                .id(p.getId())
                .payoutCode(p.getPayoutCode())
                .grossAmount(p.getGrossAmount())
                .commissionAmount(p.getCommissionAmount())
                .netAmount(p.getNetAmount())
                .qrCodeUrl(p.getQrCodeUrl())
                .proofImageUrl(p.getProofImageUrl())
                .status(p.getStatus())
                .paidAt(p.getPaidAt())
                .build();
    }

    private DisputeResponse toDisputeResponse(Dispute d) {
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
}
