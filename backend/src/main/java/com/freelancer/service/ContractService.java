package com.freelancer.service;

import com.freelancer.dto.request.CompletionRejectRequest;
import com.freelancer.dto.request.CompletionSubmitRequest;
import com.freelancer.dto.request.DisputeRequest;
import com.freelancer.dto.request.ProgressReportRequest;
import com.freelancer.dto.response.ContractDetailResponse;
import com.freelancer.dto.response.ContractListItemResponse;
import com.freelancer.dto.response.ProgressReportResponse;
import com.freelancer.entity.enums.ContractStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractService {

    /** CLIENT/FREELANCER: danh sách contract của mình (có filter status). */
    Page<ContractListItemResponse> getMyContracts(Long userId, String role,
                                                  ContractStatus statusFilter,
                                                  Pageable pageable);

    /** CLIENT/FREELANCER: chi tiết contract kèm flags và related data. */
    ContractDetailResponse getContractById(Long contractId, Long userId);

    /** CLIENT: tạo URL thanh toán VNPay. */
    String initiatePayment(Long contractId, Long clientId, HttpServletRequest request);

    /** FREELANCER: tạo báo cáo tiến độ. */
    ProgressReportResponse createProgressReport(Long contractId,
                                                ProgressReportRequest req,
                                                Long freelancerId);

    /** FREELANCER: nộp bàn giao. */
    ContractDetailResponse submitCompletion(Long contractId,
                                            CompletionSubmitRequest req,
                                            Long freelancerId);

    /** CLIENT/SYSTEM: xác nhận hoàn thành. */
    ContractDetailResponse confirmCompletion(Long contractId, Long clientId, boolean auto);

    /** CLIENT: từ chối bàn giao. */
    ContractDetailResponse rejectCompletion(Long contractId,
                                            CompletionRejectRequest req,
                                            Long clientId);

    /** CLIENT|FREELANCER: mở tranh chấp. */
    ContractDetailResponse raiseDispute(Long contractId, DisputeRequest req, Long userId);

    /** SCHEDULER: auto-cancel PENDING_PAYMENT quá 24h. */
    void autoCancelExpiredContracts();

    /** SCHEDULER: auto-confirm FREELANCER_SUBMITTED sau 7 ngày. */
    void autoConfirmCompletion();
}
