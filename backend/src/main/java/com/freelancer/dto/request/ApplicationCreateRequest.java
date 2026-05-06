package com.freelancer.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplicationCreateRequest {

    @NotBlank(message = "Thư giới thiệu không được để trống")
    @Size(min = 20, max = 2000, message = "Thư giới thiệu phải từ 20 đến 2000 ký tự")
    private String coverLetter;

    @NotNull(message = "Mức giá đề xuất là bắt buộc")
    @DecimalMin(value = "100000", message = "Giá đề xuất tối thiểu 100.000 ₫")
    private BigDecimal proposedPrice;

    @NotNull(message = "Số ngày hoàn thành là bắt buộc")
    @Positive(message = "Số ngày phải là số dương")
    private Integer estimatedDays;

    /** Optional — URL trả về từ POST /api/files/upload với subFolder=attachments */
    private String attachmentUrl;
}
