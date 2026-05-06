package com.freelancer.service.impl;

import com.freelancer.dto.request.ApplicationCreateRequest;
import com.freelancer.dto.response.ApplicationListItemResponse;
import com.freelancer.dto.response.ApplicationResponse;
import com.freelancer.dto.response.ContractResponse;
import com.freelancer.entity.*;
import com.freelancer.entity.enums.ApplicationStatus;
import com.freelancer.entity.enums.ContractStatus;
import com.freelancer.entity.enums.JobStatus;
import com.freelancer.event.ApplicationAcceptedEvent;
import com.freelancer.event.EventPublisher;
import com.freelancer.event.NewApplicationEvent;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.mapper.ApplicationMapper;
import com.freelancer.mapper.ContractMapper;
import com.freelancer.repository.*;
import com.freelancer.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final JobRepository             jobRepository;
    private final JobApplicationRepository  applicationRepository;
    private final ContractRepository        contractRepository;
    private final UserRepository            userRepository;
    private final UserProfileRepository     userProfileRepository;
    private final SystemSettingRepository   systemSettingRepository;
    private final ApplicationMapper         applicationMapper;
    private final ContractMapper            contractMapper;
    private final EventPublisher            eventPublisher;

    // -------------------------------------------------------------------------
    // FREELANCER: apply
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ApplicationResponse apply(Long jobId, ApplicationCreateRequest req, Long freelancerId) {

        // 1. Tìm job
        Job job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        // 2. Job phải đang OPEN
        if (job.getStatus() != JobStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Job không còn nhận ứng tuyển");
        }

        // 3. Không tự ứng tuyển job mình đăng
        if (job.getClientId().equals(freelancerId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Không thể tự ứng tuyển job mình đăng");
        }

        // 4. Kiểm tra đã apply chưa
        if (applicationRepository.existsByJobIdAndFreelancerId(jobId, freelancerId)) {
            throw new AppException(ErrorCode.DUPLICATE_APPLICATION);
        }

        // 5. Lưu application
        JobApplication application = JobApplication.builder()
                .jobId(jobId)
                .freelancerId(freelancerId)
                .coverLetter(req.getCoverLetter())
                .proposedBudget(req.getProposedPrice())
                .proposedDurationDays(req.getEstimatedDays())
                .attachmentUrl(req.getAttachmentUrl())
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();
        application = applicationRepository.save(application);

        // Increment applicationCount trên job
        jobRepository.incrementApplicationCount(jobId);

        User freelancer = userRepository.findById(freelancerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUserId(freelancerId).orElse(null);

        // Publish notification to client
        eventPublisher.publish("notification.user", NewApplicationEvent.builder()
                .recipientUserId(job.getClientId())
                .referenceType("Job")
                .referenceId(job.getId())
                .jobTitle(job.getTitle())
                .freelancerName(freelancer.getFullName())
                .occurredAt(LocalDateTime.now())
                .build());

        return applicationMapper.toResponse(application, freelancer, profile, job.getTitle());
    }

    // -------------------------------------------------------------------------
    // FREELANCER: withdraw
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void withdraw(Long applicationId, Long freelancerId) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!app.getFreelancerId().equals(freelancerId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chỉ có thể rút ứng tuyển khi đang ở trạng thái PENDING");
        }

        app.setStatus(ApplicationStatus.WITHDRAWN);
        app.setRespondedAt(LocalDateTime.now());
        applicationRepository.save(app);
    }

    // -------------------------------------------------------------------------
    // CLIENT: list applications for a job
    // -------------------------------------------------------------------------

    @Override
    public Page<ApplicationListItemResponse> getApplicationsByJob(Long jobId, Long clientId, Pageable pageable) {
        Job job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if (!job.getClientId().equals(clientId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Page<JobApplication> page = applicationRepository.findByJobId(jobId, pageable);
        return page.map(app -> buildListItem(app, job.getTitle()));
    }

    // -------------------------------------------------------------------------
    // FREELANCER: list my applications
    // -------------------------------------------------------------------------

    @Override
    public Page<ApplicationResponse> getMyApplications(Long freelancerId, ApplicationStatus status, Pageable pageable) {
        Page<JobApplication> page;
        if (status != null) {
            page = applicationRepository.findByFreelancerIdAndStatus(freelancerId, status, pageable);
        } else {
            page = applicationRepository.findByFreelancerId(freelancerId, pageable);
        }

        // Batch-load job titles
        List<Long> jobIds = page.getContent().stream().map(JobApplication::getJobId).distinct().toList();
        Map<Long, String> titleMap = jobRepository.findAllById(jobIds).stream()
                .collect(Collectors.toMap(Job::getId, Job::getTitle));

        User freelancer = userRepository.findById(freelancerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUserId(freelancerId).orElse(null);

        return page.map(app ->
                applicationMapper.toResponse(app, freelancer, profile, titleMap.getOrDefault(app.getJobId(), "")));
    }

    // -------------------------------------------------------------------------
    // CLIENT: accept application → create Contract
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ContractResponse acceptApplication(Long applicationId, Long clientId) {
        // 1. Tìm application
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        // 2. Lock job với PESSIMISTIC_WRITE
        Job job = jobRepository.findByIdForUpdate(app.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        // 3. Verify ownership
        if (!job.getClientId().equals(clientId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // 4. Job phải còn OPEN
        if (job.getStatus() != JobStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Job không còn ở trạng thái OPEN");
        }

        // 5. Application phải đang PENDING
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Ứng tuyển không ở trạng thái PENDING");
        }

        // 6. Đọc commission rate từ system_settings
        BigDecimal commissionRate = systemSettingRepository.findBySettingKey("COMMISSION_RATE")
                .map(s -> new BigDecimal(s.getSettingValue()))
                .orElse(BigDecimal.TEN);

        // 7. Tạo Contract
        BigDecimal agreedPrice     = app.getProposedBudget();
        BigDecimal commissionAmt   = agreedPrice.multiply(commissionRate)
                                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount       = agreedPrice.subtract(commissionAmt);

        String contractCode = "C" + System.currentTimeMillis() + (1000 + new Random().nextInt(9000));

        Contract contract = Contract.builder()
                .contractCode(contractCode)
                .jobId(job.getId())
                .applicationId(app.getId())
                .clientId(clientId)
                .freelancerId(app.getFreelancerId())
                .agreedPrice(agreedPrice)
                .commissionRate(commissionRate)
                .commissionAmount(commissionAmt)
                .netAmount(netAmount)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(app.getProposedDurationDays()))
                .status(ContractStatus.PENDING_PAYMENT)
                .build();
        contract = contractRepository.save(contract);

        // 8 & 9. Cập nhật trạng thái
        app.setStatus(ApplicationStatus.ACCEPTED);
        app.setRespondedAt(LocalDateTime.now());
        applicationRepository.save(app);

        // 10. Job → IN_PROGRESS
        job.setStatus(JobStatus.IN_PROGRESS);
        jobRepository.save(job);

        // 11. Reject các application PENDING khác cùng job
        applicationRepository.rejectOtherApplications(job.getId(), app.getId());

        // 12. Trả ContractResponse
        User clientUser     = userRepository.findById(clientId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User freelancerUser = userRepository.findById(app.getFreelancerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Publish notification to freelancer
        eventPublisher.publish("notification.user", ApplicationAcceptedEvent.builder()
                .recipientUserId(app.getFreelancerId())
                .referenceType("Contract")
                .referenceId(contract.getId())
                .jobTitle(job.getTitle())
                .occurredAt(LocalDateTime.now())
                .build());

        return contractMapper.toResponse(contract, job.getTitle(), clientUser, freelancerUser);
    }

    // -------------------------------------------------------------------------
    // CLIENT: reject application
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ApplicationResponse rejectApplication(Long applicationId, String reason, Long clientId) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        // Verify clientId via job ownership
        Job job = jobRepository.findByIdAndDeletedAtIsNull(app.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if (!job.getClientId().equals(clientId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chỉ có thể từ chối ứng tuyển đang PENDING");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setRespondedAt(LocalDateTime.now());
        applicationRepository.save(app);

        User freelancer = userRepository.findById(app.getFreelancerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUserId(app.getFreelancerId()).orElse(null);

        return applicationMapper.toResponse(app, freelancer, profile, job.getTitle());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ApplicationListItemResponse buildListItem(JobApplication app, String jobTitle) {
        User freelancer = userRepository.findById(app.getFreelancerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUserId(app.getFreelancerId()).orElse(null);
        return applicationMapper.toListItem(app, freelancer, profile, jobTitle);
    }
}
