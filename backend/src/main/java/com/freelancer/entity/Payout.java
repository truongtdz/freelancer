package com.freelancer.entity;

import com.freelancer.entity.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payout_code", unique = true, nullable = false, length = 50)
    private String payoutCode;

    @Column(name = "contract_id", unique = true)
    private Long contractId;

    @Column(name = "freelancer_id")
    private Long freelancerId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "gross_amount", precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "commission_amount", precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "net_amount", precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    // Lưu dạng JSON string — không cần hibernate-types, tránh dependency phức tạp
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bank_info_snapshot", columnDefinition = "JSONB")
    private String bankInfoSnapshot;

    @Column(name = "proof_image_url", length = 500)
    private String proofImageUrl;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
