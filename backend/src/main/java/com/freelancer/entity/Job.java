package com.freelancer.entity;

import com.freelancer.entity.enums.BudgetType;
import com.freelancer.entity.enums.JobStatus;
import com.freelancer.entity.enums.WorkMode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "budget_min", precision = 12, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", precision = 12, scale = 2)
    private BigDecimal budgetMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_type", length = 10)
    private BudgetType budgetType;

    @Column(name = "duration_days")
    private Integer durationDays;

    private LocalDate deadline;

    @Column(length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", length = 10)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private JobStatus status = JobStatus.OPEN;

    @Column(name = "view_count")
    @Builder.Default
    private int viewCount = 0;

    @Column(name = "application_count")
    @Builder.Default
    private int applicationCount = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
