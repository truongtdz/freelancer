package com.freelancer.dto.request;

import com.freelancer.entity.enums.DisputeResolutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DisputeResolveRequest {

    @NotNull(message = "resolutionType is required")
    private DisputeResolutionType resolutionType;

    /** Required when resolutionType == PARTIAL */
    private BigDecimal partialAmountToFreelancer;

    @NotBlank
    @Size(min = 10, max = 2000)
    private String resolution;
}
