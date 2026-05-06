package com.freelancer.file;

import com.freelancer.dto.response.ApiResponse;
import com.freelancer.dto.response.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<FileUploadResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String subFolder) {
        return ApiResponse.success(fileUploadService.upload(file, subFolder));
    }

    @DeleteMapping("/{subFolder}/{storedName}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> delete(
            @PathVariable String subFolder,
            @PathVariable String storedName) {
        fileUploadService.delete(storedName, subFolder);
        return ApiResponse.success("Deleted");
    }
}
