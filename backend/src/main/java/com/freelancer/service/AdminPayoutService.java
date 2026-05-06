package com.freelancer.service;

import com.freelancer.dto.request.PayoutCreateRequest;
import com.freelancer.dto.response.PayoutResponse;
import com.freelancer.dto.response.PendingPayoutItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminPayoutService {

    Page<PendingPayoutItem> getPendingPayouts(Pageable pageable);

    PayoutResponse payout(Long contractId, PayoutCreateRequest req, Long adminId);
}
