package com.freelancer.mapper;

import com.freelancer.dto.response.SkillResponse;
import com.freelancer.entity.Skill;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillResponse toResponse(Skill skill);
    List<SkillResponse> toResponseList(List<Skill> skills);
    Set<SkillResponse> toResponseSet(Set<Skill> skills);
}
