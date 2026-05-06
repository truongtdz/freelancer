package com.freelancer.dto.response;

import com.freelancer.entity.enums.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long contractId;
    private UserSummaryResponse reviewer;
    private UserSummaryResponse reviewee;
    private Integer rating;
    private String comment;
    private ReviewType reviewType;
    private LocalDateTime createdAt;
}
