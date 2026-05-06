package com.freelancer.service;

import com.freelancer.dto.request.JobCreateRequest;
import com.freelancer.dto.request.JobSearchRequest;
import com.freelancer.dto.request.JobUpdateRequest;
import com.freelancer.dto.response.JobDetailResponse;
import com.freelancer.dto.response.JobListItemResponse;
import com.freelancer.entity.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {
    JobDetailResponse createJob(JobCreateRequest req, Long clientId);
    Page<JobListItemResponse> getJobs(JobSearchRequest req, Pageable pageable, Long currentUserId);
    JobDetailResponse getJobById(Long id, Long currentUserId);
    JobDetailResponse updateJob(Long id, JobUpdateRequest req, Long clientId);
    void deleteJob(Long id, Long clientId);
    Page<JobListItemResponse> getMyJobs(Long clientId, Pageable pageable, JobStatus status);
    JobDetailResponse closeJob(Long id, Long clientId);
}
