package com.freelancer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotBlank
    @Size(max = 4000)
    private String content;

    private String attachmentUrl;
}
