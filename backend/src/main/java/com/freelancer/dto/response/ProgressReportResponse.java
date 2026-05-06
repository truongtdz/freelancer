package com.freelancer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressReportResponse {

    private Long id;
    private Long contractId;
    private String title;
    private String content;
    private Integer progressPercentage;
    private List<String> attachmentUrls;
    private String clientFeedback;
    private LocalDateTime createdAt;
}
