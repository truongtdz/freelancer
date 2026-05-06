package com.freelancer.dto.response;

import com.freelancer.entity.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractListItemResponse {

    private Long id;
    private String contractCode;
    private Long jobId;
    private String jobTitle;

    /** Counterparty: CLIENT sees freelancer, FREELANCER sees client */
    private UserSummaryResponse counterparty;

    private BigDecimal agreedPrice;
    private ContractStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime updatedAt;
}
