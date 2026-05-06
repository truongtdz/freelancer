package com.freelancer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayoutCreateRequest {

    @NotBlank(message = "proofImageUrl is required")
    private String proofImageUrl;

    private String note;
}
