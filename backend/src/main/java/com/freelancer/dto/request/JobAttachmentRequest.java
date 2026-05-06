package com.freelancer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobAttachmentRequest {
    @NotBlank String fileUrl;
    @NotBlank String fileName;
    @NotNull long fileSize;
}
