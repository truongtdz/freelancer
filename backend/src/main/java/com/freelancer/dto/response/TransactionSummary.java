package com.freelancer.dto.response;

import com.freelancer.entity.enums.TransactionStatus;
import com.freelancer.entity.enums.TransactionType;
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
public class TransactionSummary {

    private Long id;
    private String transactionCode;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
