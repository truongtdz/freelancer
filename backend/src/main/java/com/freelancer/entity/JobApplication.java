package com.freelancer.entity;

import com.freelancer.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "job_applications",
    uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "freelancer_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "proposed_budget", precision = 12, scale = 2)
    private BigDecimal proposedBudget;

    @Column(name = "proposed_duration_days")
    private Integer proposedDurationDays;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}
