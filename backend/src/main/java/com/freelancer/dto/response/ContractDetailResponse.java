package com.freelancer.dto.response;

import com.freelancer.entity.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDetailResponse {

    private Long id;
    private String contractCode;

    private Long jobId;
    private String jobTitle;

    private UserSummaryResponse client;
    private UserSummaryResponse freelancer;

    private BigDecimal agreedPrice;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;

    private LocalDate startDate;
    private LocalDate endDate;

    private ContractStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related data
    private List<ProgressReportResponse>      progressReports;
    private List<CompletionSubmissionResponse> completionSubmissions;
    private List<TransactionSummary>          transactions;
    private PayoutResponse                    payout;
    private DisputeResponse                   dispute;

    // Permission flags (computed for requesting user)
    private boolean canPay;
    private boolean canReportProgress;
    private boolean canSubmitCompletion;
    private boolean canConfirmCompletion;
    private boolean canRejectCompletion;
    private boolean canRaiseDispute;

    private int submissionAttempts;
    private int maxSubmissionAttempts;
}
