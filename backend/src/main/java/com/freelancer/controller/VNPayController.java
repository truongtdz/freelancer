package com.freelancer.controller;

import com.freelancer.payment.VNPayService;
import com.freelancer.payment.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
public class VNPayController {

    private final VNPayService vnPayService;

    /**
     * VNPay IPN — server-to-server, KHÔNG cần auth.
     * VNPay gọi GET với query params; phải trả JSON {"RspCode":"00","Message":"..."}
     */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipn(HttpServletRequest request) {
        Map<String, String> params = VNPayUtil.extractParams(request.getParameterMap());
        log.info("VNPay IPN received: txnRef={}", params.get("vnp_TxnRef"));
        Map<String, String> response = vnPayService.handleIpn(params);
        return ResponseEntity.ok(response);
    }

    /**
     * VNPay Return URL — redirect user sau khi thanh toán.
     * Chỉ verify chữ ký rồi redirect về FE; KHÔNG đổi state (IPN đã xử lý).
     */
    @GetMapping("/return")
    public ResponseEntity<Void> returnUrl(HttpServletRequest request) {
        Map<String, String> params = VNPayUtil.extractParams(request.getParameterMap());
        log.info("VNPay Return received: txnRef={}, code={}", params.get("vnp_TxnRef"), params.get("vnp_ResponseCode"));
        String redirectUrl = vnPayService.buildReturnRedirect(params);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }
}
