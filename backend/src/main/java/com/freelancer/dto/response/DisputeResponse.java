package com.freelancer.dto.response;

import com.freelancer.entity.enums.DisputeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeResponse {

    private Long id;
    private String reason;
    private String description;
    private DisputeStatus status;
    private String resolution;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
