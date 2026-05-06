package com.freelancer.entity;

import com.freelancer.entity.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_code", unique = true, nullable = false, length = 50)
    private String contractCode;

    @Column(name = "job_id", unique = true)
    private Long jobId;

    @Column(name = "application_id", unique = true)
    private Long applicationId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId;

    @Column(name = "agreed_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal agreedPrice;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "net_amount", precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ContractStatus status = ContractStatus.PENDING_PAYMENT;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
