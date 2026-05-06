package com.freelancer.dto.response;

import com.freelancer.entity.enums.PayoutStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutResponse {

    private Long id;
    private String payoutCode;

    // contract info (populated in admin context)
    private Long contractId;
    private String contractCode;

    // parties (populated in admin context)
    private UserSummaryResponse freelancer;
    private UserSummaryResponse admin;

    private BigDecimal grossAmount;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;
    private String qrCodeUrl;
    private String bankInfoSnapshot;
    private String proofImageUrl;
    private PayoutStatus status;
    private LocalDateTime paidAt;
}
