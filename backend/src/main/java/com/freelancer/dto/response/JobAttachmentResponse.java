package com.freelancer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JobAttachmentResponse {
    private Long id;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
}
