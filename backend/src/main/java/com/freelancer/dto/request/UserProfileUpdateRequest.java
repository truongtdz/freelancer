package com.freelancer.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class UserProfileUpdateRequest {

    @Size(max = 200)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 2000)
    private String bio;

    @Size(max = 200)
    private String title;

    @Min(0)
    @Max(70)
    private Integer experienceYears;

    @DecimalMin("0")
    private BigDecimal hourlyRate;

    /** Only processed for FREELANCER role */
    private Set<UserSkillRequest> skills;
}
