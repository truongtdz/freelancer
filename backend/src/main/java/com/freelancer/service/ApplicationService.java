package com.freelancer.service;

import com.freelancer.dto.request.ApplicationCreateRequest;
import com.freelancer.dto.response.ApplicationListItemResponse;
import com.freelancer.dto.response.ApplicationResponse;
import com.freelancer.dto.response.ContractResponse;
import com.freelancer.entity.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {

    /** FREELANCER: nộp ứng tuyển cho job */
    ApplicationResponse apply(Long jobId, ApplicationCreateRequest req, Long freelancerId);

    /** FREELANCER: rút ứng tuyển (chỉ khi PENDING) */
    void withdraw(Long applicationId, Long freelancerId);

    /** CLIENT: xem danh sách ứng tuyển của job mình đăng */
    Page<ApplicationListItemResponse> getApplicationsByJob(Long jobId, Long clientId, Pageable pageable);

    /** FREELANCER: xem danh sách ứng tuyển của mình */
    Page<ApplicationResponse> getMyApplications(Long freelancerId, ApplicationStatus status, Pageable pageable);

    /** CLIENT: chấp nhận ứng tuyển → tạo Contract PENDING_PAYMENT */
    ContractResponse acceptApplication(Long applicationId, Long clientId);

    /** CLIENT: từ chối ứng tuyển */
    ApplicationResponse rejectApplication(Long applicationId, String reason, Long clientId);
}
