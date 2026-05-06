package com.freelancer.dto.response;

import com.freelancer.entity.enums.BudgetType;
import com.freelancer.entity.enums.JobStatus;
import com.freelancer.entity.enums.WorkMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JobDetailResponse {
    private Long id;
    private String title;
    private String shortDescription;
    private String description;
    private CategoryResponse category;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private BudgetType budgetType;
    private WorkMode workMode;
    private JobStatus status;
    private String location;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummaryResponse client;
    private Set<SkillResponse> skills;
    private List<JobAttachmentResponse> attachments;
    private int applicationCount;
    private boolean canApply;
    private boolean owner;
}
