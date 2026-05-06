package com.freelancer.entity;

import com.freelancer.entity.enums.PaymentMethod;
import com.freelancer.entity.enums.TransactionStatus;
import com.freelancer.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", unique = true, nullable = false, length = 50)
    private String transactionCode;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    @Builder.Default
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "vnp_txn_ref", unique = true, length = 100)
    private String vnpTxnRef;

    @Column(name = "vnp_transaction_no", length = 100)
    private String vnpTransactionNo;

    @Column(name = "vnp_bank_code", length = 20)
    private String vnpBankCode;

    @Column(name = "vnp_response_code", length = 10)
    private String vnpResponseCode;

    @Column(name = "vnp_response", columnDefinition = "TEXT")
    private String vnpResponse;

    @Column(name = "processed_by_admin_id")
    private Long processedByAdminId;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
