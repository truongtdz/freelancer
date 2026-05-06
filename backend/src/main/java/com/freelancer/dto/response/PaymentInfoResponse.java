package com.freelancer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoResponse {

    private Long id;
    private String bankName;
    private String bankAccountNumber;   // masked: ****1234
    private String bankAccountHolder;
    private String qrCodeUrl;
    @JsonProperty("isDefault")
    private boolean isDefault;
}
