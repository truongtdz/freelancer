package com.freelancer.dto.request;

import com.freelancer.entity.enums.BudgetType;
import com.freelancer.entity.enums.WorkMode;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class JobCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    private String description;

    @NotNull
    private Long categoryId;

    @NotNull
    @DecimalMin("100000")
    private BigDecimal budgetMin;

    @NotNull
    @DecimalMin("100000")
    private BigDecimal budgetMax;

    @NotNull
    private BudgetType budgetType;

    private LocalDate deadline;

    @NotNull
    private WorkMode workMode;

    private String location;

    @NotEmpty
    private Set<Long> skillIds;

    private List<JobAttachmentRequest> attachments;
}
