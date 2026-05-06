package com.freelancer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DisputeRequest {

    @NotBlank(message = "Lý do tranh chấp không được để trống")
    @Size(min = 10, max = 200, message = "Lý do từ 10 đến 200 ký tự")
    private String reason;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 20, max = 2000, message = "Mô tả từ 20 đến 2000 ký tự")
    private String description;
}
