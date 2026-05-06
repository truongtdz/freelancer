package com.freelancer.controller;

import com.freelancer.dto.request.JobCreateRequest;
import com.freelancer.dto.request.JobSearchRequest;
import com.freelancer.dto.request.JobUpdateRequest;
import com.freelancer.dto.response.ApiResponse;
import com.freelancer.dto.response.JobDetailResponse;
import com.freelancer.dto.response.JobListItemResponse;
import com.freelancer.entity.enums.JobStatus;
import com.freelancer.security.CustomUserDetails;
import com.freelancer.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<JobDetailResponse> createJob(
            @Valid @RequestBody JobCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.success(jobService.createJob(req, principal.getId()));
    }

    @GetMapping
    public ApiResponse<Page<JobListItemResponse>> getJobs(
            @ModelAttribute JobSearchRequest req,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ApiResponse.success(jobService.getJobs(req, pageable, userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<JobDetailResponse> getJobById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ApiResponse.success(jobService.getJobById(id, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<JobDetailResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.success(jobService.updateJob(id, req, principal.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<String> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        jobService.deleteJob(id, principal.getId());
        return ApiResponse.success("Job đã được xoá");
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<Page<JobListItemResponse>> getMyJobs(
            @RequestParam(required = false) JobStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.success(jobService.getMyJobs(principal.getId(), pageable, status));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<JobDetailResponse> closeJob(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.success(jobService.closeJob(id, principal.getId()));
    }
}
