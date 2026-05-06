package com.freelancer.entity;

import com.freelancer.entity.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "completion_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletionSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NOT unique — 1 contract có nhiều submission (attempt 1, 2, 3...)
    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "freelancer_id")
    private Long freelancerId;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "deliverables_url", columnDefinition = "TEXT")
    private String deliverablesUrl;

    @Column(name = "payment_info_id")
    private Long paymentInfoId;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "client_confirmed_at")
    private LocalDateTime clientConfirmedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING_CONFIRM;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "attempt_number", nullable = false)
    @Builder.Default
    private int attemptNumber = 1;
}
