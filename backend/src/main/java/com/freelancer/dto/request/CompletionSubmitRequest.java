package com.freelancer.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CompletionSubmitRequest {

    @NotBlank(message = "Tóm tắt không được để trống")
    @Size(min = 20, max = 2000, message = "Tóm tắt từ 20 đến 2000 ký tự")
    private String summary;

    @NotEmpty(message = "Cần ít nhất 1 link bàn giao")
    private List<String> deliverableUrls;

    /** Optional — chọn payment info cụ thể để lấy QR code */
    private Long paymentInfoId;

    /** Optional — override QR code URL */
    private String qrCodeUrl;
}
