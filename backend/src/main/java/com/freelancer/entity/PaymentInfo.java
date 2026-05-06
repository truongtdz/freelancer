package com.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_account_holder", length = 100)
    private String bankAccountHolder;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    @Column(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
