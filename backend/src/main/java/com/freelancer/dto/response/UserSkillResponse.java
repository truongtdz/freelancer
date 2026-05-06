package com.freelancer.dto.response;

import com.freelancer.entity.enums.SkillLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSkillResponse {

    private Long skillId;
    private String name;
    private String slug;
    private SkillLevel level;
}
