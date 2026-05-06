package com.freelancer.dto.response;

import com.freelancer.entity.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Gọn hơn ApplicationResponse — dùng cho danh sách, không chứa toàn bộ coverLetter.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationListItemResponse {

    private Long id;
    private Long jobId;
    private String jobTitle;

    private UserSummaryResponse freelancer;
    private Double freelancerRating;
    private Integer freelancerCompletedJobs;

    /** Preview 150 ký tự đầu của cover letter */
    private String coverLetterPreview;
    private BigDecimal proposedPrice;
    private Integer estimatedDays;
    private String attachmentUrl;

    private ApplicationStatus status;
    private LocalDateTime createdAt;
}
