package com.freelancer.dto.request;

import lombok.Data;

@Data
public class AiCoverLetterRequest {
    private String jobTitle;
    private String jobDescription;
    private Long   budgetMin;
    private Long   budgetMax;
}
