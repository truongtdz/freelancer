package com.freelancer.file;

import com.freelancer.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    FileUploadResponse upload(MultipartFile file, String subFolder);
    void delete(String storedName, String subFolder);
    boolean isAllowedSubFolder(String subFolder);
    boolean isAllowedContentType(String contentType, String subFolder);
}
