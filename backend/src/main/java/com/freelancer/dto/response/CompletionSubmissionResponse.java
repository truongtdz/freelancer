package com.freelancer.dto.response;

import com.freelancer.entity.enums.SubmissionStatus;
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
public class CompletionSubmissionResponse {

    private Long id;
    private Long contractId;
    private String summary;
    private List<String> deliverableUrls;
    private String qrCodeUrl;
    private SubmissionStatus status;
    private String rejectionReason;
    private int attemptNumber;
    private LocalDateTime createdAt;
}
