package com.freelancer.dto.request;

import com.freelancer.entity.enums.BudgetType;
import com.freelancer.entity.enums.WorkMode;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class JobSearchRequest {
    private String keyword;
    private Long categoryId;
    private Set<Long> skillIds;
    private BudgetType budgetType;
    private WorkMode workMode;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String sort = "createdAt,desc";
}
