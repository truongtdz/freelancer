package com.freelancer.dto.request;

import com.freelancer.entity.enums.SkillLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserSkillRequest {

    @NotNull
    private Long skillId;

    @NotNull
    private SkillLevel level;
}
