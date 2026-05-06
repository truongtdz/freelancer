package com.freelancer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String url;
    private String fileName;
    private String storedName;
    private long fileSize;
    private String contentType;
    private String subFolder;
}
