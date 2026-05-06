package com.freelancer.entity;

import com.freelancer.entity.enums.ProgressReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ProgressReportStatus status = ProgressReportStatus.SUBMITTED;

    @Column(name = "client_feedback", columnDefinition = "TEXT")
    private String clientFeedback;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;
}
