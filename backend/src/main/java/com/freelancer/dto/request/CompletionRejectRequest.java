package com.freelancer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompletionRejectRequest {

    @NotBlank(message = "Lý do từ chối không được để trống")
    @Size(min = 10, max = 1000, message = "Lý do từ 10 đến 1000 ký tự")
    private String reason;
}
