package com.freelancer.payment;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VNPayService {

    /**
     * Tạo URL thanh toán VNPay cho contract.
     *
     * @param contractId ID của contract (phải PENDING_PAYMENT)
     * @param clientId   ID của client (phải là chủ contract)
     * @param request    HttpServletRequest để lấy IP
     * @return URL thanh toán VNPay
     */
    String createPaymentUrl(Long contractId, Long clientId, HttpServletRequest request);

    /**
     * Xử lý IPN callback từ VNPay (GET /api/vnpay/ipn).
     * Đây là nguồn sự thật duy nhất để đổi trạng thái.
     *
     * @param params tất cả query params từ VNPay
     * @return JSON response theo chuẩn VNPay: {"RspCode":"00","Message":"..."}
     */
    Map<String, String> handleIpn(Map<String, String> params);

    /**
     * Xử lý Return URL từ VNPay (GET /api/vnpay/return).
     * Cũng cập nhật state idempotently (phòng trường hợp IPN không tới được — môi trường local/dev).
     *
     * @param params tất cả query params từ VNPay
     * @return redirect URL tới FE (success hoặc cancel)
     */
    String buildReturnRedirect(Map<String, String> params);
}
