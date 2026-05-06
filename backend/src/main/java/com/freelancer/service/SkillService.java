package com.freelancer.service;

import com.freelancer.dto.response.SkillResponse;

import java.util.List;

public interface SkillService {
    List<SkillResponse> getAll();
    List<SkillResponse> search(String keyword);
}
