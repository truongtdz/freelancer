package com.freelancer.service;

import com.freelancer.dto.request.PaymentInfoRequest;
import com.freelancer.dto.request.UserProfileUpdateRequest;
import com.freelancer.dto.response.PaymentInfoResponse;
import com.freelancer.dto.response.UserProfileResponse;

import java.util.List;

public interface UserService {

    UserProfileResponse getProfile(Long userId);

    UserProfileResponse updateMyProfile(Long userId, UserProfileUpdateRequest req);

    List<PaymentInfoResponse> getMyPaymentInfos(Long userId);

    PaymentInfoResponse savePaymentInfo(Long userId, PaymentInfoRequest req);

    void deletePaymentInfo(Long userId, Long paymentInfoId);
}
