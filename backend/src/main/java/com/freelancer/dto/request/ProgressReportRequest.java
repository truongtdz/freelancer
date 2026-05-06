package com.freelancer.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ProgressReportRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 2000, message = "Nội dung tối đa 2000 ký tự")
    private String content;

    @NotNull(message = "Phần trăm hoàn thành không được để trống")
    @Min(value = 0, message = "Tối thiểu 0%")
    @Max(value = 100, message = "Tối đa 100%")
    private Integer progressPercentage;

    /** URL file đính kèm — optional, lưu dạng CSV trong DB */
    private List<String> attachmentUrls;
}
