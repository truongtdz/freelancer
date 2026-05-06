package com.freelancer.service;

import com.freelancer.dto.request.DisputeResolveRequest;
import com.freelancer.dto.response.DisputeResponse;
import com.freelancer.entity.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminDisputeService {

    Page<DisputeResponse> getDisputes(DisputeStatus status, Pageable pageable);

    DisputeResponse resolveDispute(Long disputeId, DisputeResolveRequest req, Long adminId);
}
