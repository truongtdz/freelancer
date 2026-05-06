package com.freelancer.service.impl;

import com.freelancer.dto.request.JobAttachmentRequest;
import com.freelancer.dto.request.JobCreateRequest;
import com.freelancer.dto.request.JobSearchRequest;
import com.freelancer.dto.request.JobUpdateRequest;
import com.freelancer.dto.response.JobDetailResponse;
import com.freelancer.dto.response.JobListItemResponse;
import com.freelancer.entity.*;
import com.freelancer.entity.enums.ApplicationStatus;
import com.freelancer.entity.enums.JobStatus;
import com.freelancer.entity.enums.UserRole;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.mapper.JobMapper;
import com.freelancer.repository.*;
import com.freelancer.service.JobService;
import com.freelancer.service.JobSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final JobAttachmentRepository jobAttachmentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final JobMapper jobMapper;

    private static final String KEY_MAX_ATTACHMENTS = "JOB_MAX_ATTACHMENTS";
    private static final int DEFAULT_MAX_ATTACHMENTS = 5;

    @Override
    @Transactional
    public JobDetailResponse createJob(JobCreateRequest req, Long clientId) {
        if (req.getBudgetMin().compareTo(req.getBudgetMax()) > 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "budgetMin phải <= budgetMax");
        }

        categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        List<Skill> skills = skillRepository.findAllById(req.getSkillIds());
        if (skills.size() != req.getSkillIds().size()) {
            throw new AppException(ErrorCode.SKILL_NOT_FOUND);
        }

        int maxAtt = getMaxAttachments();
        if (req.getAttachments() != null && req.getAttachments().size() > maxAtt) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Tối đa " + maxAtt + " file đính kèm");
        }

        Job job = jobMapper.toEntity(req, clientId);
        job = jobRepository.save(job);
        final Long jobId = job.getId();

        for (Long skillId : req.getSkillIds()) {
            jobSkillRepository.save(JobSkill.builder().jobId(jobId).skillId(skillId).build());
        }

        if (req.getAttachments() != null) {
            for (JobAttachmentRequest a : req.getAttachments()) {
                jobAttachmentRepository.save(JobAttachment.builder()
                        .jobId(jobId).fileUrl(a.getFileUrl())
                        .fileName(a.getFileName()).fileSize(a.getFileSize())
                        .uploadedAt(LocalDateTime.now()).build());
            }
        }

        return buildDetail(job, clientId, false, false);
    }

    @Override
    public Page<JobListItemResponse> getJobs(JobSearchRequest req, Pageable pageable, Long currentUserId) {
        Page<Job> page = jobRepository.findAll(JobSpecification.build(req), pageable);

        List<Long> jobIds = page.getContent().stream().map(Job::getId).toList();
        if (jobIds.isEmpty()) return page.map(j -> null);

        // Batch load clients, categories, skills
        List<Long> clientIds = page.getContent().stream().map(Job::getClientId).distinct().toList();
        List<Long> categoryIds = page.getContent().stream()
                .map(Job::getCategoryId).filter(Objects::nonNull).distinct().toList();

        Map<Long, User> clientMap = userRepository.findAllById(clientIds)
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream().collect(Collectors.toMap(Category::getId, Function.identity()));

        // Skills: jobSkills grouped by jobId, then load all skills once
        List<JobSkill> allJobSkills = jobSkillRepository.findByJobIdIn(jobIds);
        Map<Long, List<Long>> jobSkillMap = allJobSkills.stream()
                .collect(Collectors.groupingBy(JobSkill::getJobId,
                        Collectors.mapping(JobSkill::getSkillId, Collectors.toList())));
        Set<Long> allSkillIds = allJobSkills.stream().map(JobSkill::getSkillId).collect(Collectors.toSet());
        Map<Long, Skill> skillMap = skillRepository.findAllById(allSkillIds)
                .stream().collect(Collectors.toMap(Skill::getId, Function.identity()));

        // Application counts batch
        Map<Long, Long> appCountMap = new HashMap<>();
        jobApplicationRepository.countPendingByJobIdIn(jobIds)
                .forEach(row -> appCountMap.put((Long) row[0], (Long) row[1]));

        return page.map(job -> {
            User client = clientMap.get(job.getClientId());
            Category category = job.getCategoryId() != null ? categoryMap.get(job.getCategoryId()) : null;
            Set<Skill> skills = jobSkillMap.getOrDefault(job.getId(), List.of())
                    .stream().map(skillMap::get).filter(Objects::nonNull).collect(Collectors.toSet());
            long appCount = appCountMap.getOrDefault(job.getId(), 0L);
            return jobMapper.toListItem(job, client, category, skills, appCount);
        });
    }

    @Override
    public JobDetailResponse getJobById(Long id, Long currentUserId) {
        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        boolean owner = currentUserId != null && currentUserId.equals(job.getClientId());
        boolean canApply = false;

        if (currentUserId != null && !owner) {
            User u = userRepository.findById(currentUserId).orElse(null);
            if (u != null && u.getRole() == UserRole.FREELANCER
                    && job.getStatus() == JobStatus.OPEN
                    && !jobApplicationRepository.existsByJobIdAndFreelancerId(id, currentUserId)) {
                canApply = true;
            }
        }

        return buildDetail(job, currentUserId, canApply, owner);
    }

    @Override
    @Transactional
    public JobDetailResponse updateJob(Long id, JobUpdateRequest req, Long clientId) {
        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if (!job.getClientId().equals(clientId)) {
            throw new AppException(ErrorCode.JOB_NOT_OWNED);
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ sửa được job đang OPEN");
        }

        if (StringUtils.hasText(req.getTitle()))       job.setTitle(req.getTitle());
        if (StringUtils.hasText(req.getDescription())) job.setDescription(req.getDescription());
        if (req.getCategoryId() != null) {
            categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            job.setCategoryId(req.getCategoryId());
        }
        if (req.getBudgetMin() != null) job.setBudgetMin(req.getBudgetMin());
        if (req.getBudgetMax() != null) job.setBudgetMax(req.getBudgetMax());
        if (job.getBudgetMin() != null && job.getBudgetMax() != null
                && job.getBudgetMin().compareTo(job.getBudgetMax()) > 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "budgetMin phải <= budgetMax");
        }
        if (req.getBudgetType() != null) job.setBudgetType(req.getBudgetType());
        if (req.getDeadline() != null)   job.setDeadline(req.getDeadline());
        if (req.getWorkMode() != null)   job.setWorkMode(req.getWorkMode());
        if (req.getLocation() != null)   job.setLocation(req.getLocation());

        if (req.getSkillIds() != null && !req.getSkillIds().isEmpty()) {
            jobSkillRepository.deleteByJobId(id);
            for (Long skillId : req.getSkillIds()) {
                jobSkillRepository.save(JobSkill.builder().jobId(id).skillId(skillId).build());
            }
        }

        if (req.getAttachments() != null) {
            int maxAtt = getMaxAttachments();
            if (req.getAttachments().size() > maxAtt) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Tối đa " + maxAtt + " file đính kèm");
            }
            jobAttachmentRepository.deleteByJobId(id);
            for (JobAttachmentRequest a : req.getAttachments()) {
                jobAttachmentRepository.save(JobAttachment.builder()
                        .jobId(id).fileUrl(a.getFileUrl())
                        .fileName(a.getFileName()).fileSize(a.getFileSize())
                        .uploadedAt(LocalDateTime.now()).build());
            }
        }

        job = jobRepository.save(job);
        return buildDetail(job, clientId, false, true);
    }

    @Override
    @Transactional
    public void deleteJob(Long id, Long clientId) {
        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if (!job.getClientId().equals(clientId)) {
            throw new AppException(ErrorCode.JOB_NOT_OWNED);
        }
        if (job.getStatus() == JobStatus.IN_PROGRESS || job.getStatus() == JobStatus.COMPLETED) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Job đã có hợp đồng, không thể xoá");
        }

        job.setDeletedAt(LocalDateTime.now());
        job.setStatus(JobStatus.CANCELLED);
        jobRepository.save(job);
    }

    @Override
    public Page<JobListItemResponse> getMyJobs(Long clientId, Pageable pageable, JobStatus status) {
        Page<Job> page = (status != null)
                ? jobRepository.findByClientIdAndStatusAndDeletedAtIsNull(clientId, status, pageable)
                : jobRepository.findByClientIdAndDeletedAtIsNull(clientId, pageable);

        List<Long> jobIds = page.getContent().stream().map(Job::getId).toList();
        if (jobIds.isEmpty()) return page.map(j -> null);

        List<Long> categoryIds = page.getContent().stream()
                .map(Job::getCategoryId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream().collect(Collectors.toMap(Category::getId, Function.identity()));

        User client = userRepository.findById(clientId).orElse(null);

        List<JobSkill> allJobSkills = jobSkillRepository.findByJobIdIn(jobIds);
        Map<Long, List<Long>> jobSkillMap = allJobSkills.stream()
                .collect(Collectors.groupingBy(JobSkill::getJobId,
                        Collectors.mapping(JobSkill::getSkillId, Collectors.toList())));
        Set<Long> allSkillIds = allJobSkills.stream().map(JobSkill::getSkillId).collect(Collectors.toSet());
        Map<Long, Skill> skillMap = skillRepository.findAllById(allSkillIds)
                .stream().collect(Collectors.toMap(Skill::getId, Function.identity()));

        Map<Long, Long> appCountMap = new HashMap<>();
        jobApplicationRepository.countPendingByJobIdIn(jobIds)
                .forEach(row -> appCountMap.put((Long) row[0], (Long) row[1]));

        return page.map(job -> {
            Category category = job.getCategoryId() != null ? categoryMap.get(job.getCategoryId()) : null;
            Set<Skill> skills = jobSkillMap.getOrDefault(job.getId(), List.of())
                    .stream().map(skillMap::get).filter(Objects::nonNull).collect(Collectors.toSet());
            long appCount = appCountMap.getOrDefault(job.getId(), 0L);
            return jobMapper.toListItem(job, client, category, skills, appCount);
        });
    }

    @Override
    @Transactional
    public JobDetailResponse closeJob(Long id, Long clientId) {
        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if (!job.getClientId().equals(clientId)) {
            throw new AppException(ErrorCode.JOB_NOT_OWNED);
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ đóng được job đang OPEN");
        }

        job.setStatus(JobStatus.CANCELLED);
        job = jobRepository.save(job);
        return buildDetail(job, clientId, false, true);
    }

    // ---- helpers ----

    private JobDetailResponse buildDetail(Job job, Long currentUserId, boolean canApply, boolean isOwner) {
        User client = userRepository.findById(job.getClientId()).orElse(null);
        Category category = job.getCategoryId() != null
                ? categoryRepository.findById(job.getCategoryId()).orElse(null) : null;

        Set<Long> skillIds = jobSkillRepository.findByJobId(job.getId())
                .stream().map(JobSkill::getSkillId).collect(Collectors.toSet());
        Set<Skill> skills = new HashSet<>(skillRepository.findAllById(skillIds));
        List<JobAttachment> attachments = jobAttachmentRepository.findByJobId(job.getId());
        long appCount = jobApplicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.PENDING);

        return jobMapper.toDetail(job, client, category, skills, attachments, appCount, canApply, isOwner);
    }

    private int getMaxAttachments() {
        return systemSettingRepository.findBySettingKey(KEY_MAX_ATTACHMENTS)
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(DEFAULT_MAX_ATTACHMENTS);
    }
}
