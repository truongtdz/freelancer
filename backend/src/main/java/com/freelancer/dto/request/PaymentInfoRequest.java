package com.freelancer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentInfoRequest {

    @NotBlank
    private String bankName;

    @NotBlank
    private String bankAccountNumber;

    @NotBlank
    private String bankAccountHolder;

    private String qrCodeUrl;

    private boolean isDefault;
}
