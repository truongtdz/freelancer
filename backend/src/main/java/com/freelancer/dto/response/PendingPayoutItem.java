package com.freelancer.dto.response;

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
public class PendingPayoutItem {

    private Long contractId;
    private String contractCode;
    private UserSummaryResponse freelancer;
    private UserSummaryResponse client;
    private BigDecimal agreedPrice;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;
    private String qrCodeUrl;
    private String bankInfoSnapshot;
    private LocalDateTime confirmedAt;
}
