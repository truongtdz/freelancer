package com.freelancer.dto.response;

import com.freelancer.entity.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Placeholder ContractResponse — task17 sẽ mở rộng thêm các field chi tiết.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {

    private Long id;
    private String contractCode;

    private Long jobId;
    private String jobTitle;

    private UserSummaryResponse client;
    private UserSummaryResponse freelancer;

    private BigDecimal agreedPrice;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;

    private LocalDate startDate;
    private LocalDate endDate;

    private ContractStatus status;
    private LocalDateTime createdAt;
}
