package com.freelancer.dto.response;

import com.freelancer.entity.enums.ApplicationStatus;
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
public class ApplicationResponse {

    private Long id;
    private Long jobId;
    private String jobTitle;

    private UserSummaryResponse freelancer;
    private Double freelancerRating;
    private Integer freelancerCompletedJobs;

    private String coverLetter;
    private BigDecimal proposedPrice;
    private Integer estimatedDays;
    private String attachmentUrl;

    private ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
