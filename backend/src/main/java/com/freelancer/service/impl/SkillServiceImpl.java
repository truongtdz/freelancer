package com.freelancer.service.impl;

import com.freelancer.dto.response.SkillResponse;
import com.freelancer.mapper.SkillMapper;
import com.freelancer.repository.SkillRepository;
import com.freelancer.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public List<SkillResponse> getAll() {
        return skillRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(s -> s.getName()))
                .map(skillMapper::toResponse)
                .toList();
    }

    @Override
    public List<SkillResponse> search(String keyword) {
        if (!StringUtils.hasText(keyword)) return getAll();
        return skillRepository.findByNameContainingIgnoreCase(keyword.trim())
                .stream()
                .map(skillMapper::toResponse)
                .toList();
    }
}
