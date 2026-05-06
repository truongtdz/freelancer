package com.freelancer.controller;

import com.freelancer.dto.response.ApiResponse;
import com.freelancer.dto.response.SkillResponse;
import com.freelancer.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ApiResponse<List<SkillResponse>> getAll() {
        return ApiResponse.success(skillService.getAll());
    }

    @GetMapping("/search")
    public ApiResponse<List<SkillResponse>> search(@RequestParam(defaultValue = "") String q) {
        return ApiResponse.success(skillService.search(q));
    }
}
