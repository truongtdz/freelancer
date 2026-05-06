package com.freelancer.dto.request;

import com.freelancer.entity.enums.BudgetType;
import com.freelancer.entity.enums.WorkMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class JobUpdateRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    private Long categoryId;

    @DecimalMin("100000")
    private BigDecimal budgetMin;

    @DecimalMin("100000")
    private BigDecimal budgetMax;

    private BudgetType budgetType;

    private LocalDate deadline;

    private WorkMode workMode;

    private String location;

    private Set<Long> skillIds;

    private List<JobAttachmentRequest> attachments;
}
