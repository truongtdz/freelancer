package com.freelancer.controller;

import com.freelancer.dto.request.PaymentInfoRequest;
import com.freelancer.dto.request.UserProfileUpdateRequest;
import com.freelancer.dto.response.ApiResponse;
import com.freelancer.dto.response.PaymentInfoResponse;
import com.freelancer.dto.response.UserProfileResponse;
import com.freelancer.service.UserService;
import com.freelancer.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** GET /api/users/me — authenticated, trả về profile của user hiện tại */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }

    /** GET /api/users/{id} — public */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(id)));
    }

    /** PUT /api/users/me/profile — authenticated */
    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UserProfileUpdateRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(userService.updateMyProfile(userId, req)));
    }

    /** GET /api/users/me/payment-info — authenticated */
    @GetMapping("/me/payment-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PaymentInfoResponse>>> getMyPaymentInfos() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(userService.getMyPaymentInfos(userId)));
    }

    /** POST /api/users/me/payment-info — authenticated */
    @PostMapping("/me/payment-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentInfoResponse>> savePaymentInfo(
            @Valid @RequestBody PaymentInfoRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(userService.savePaymentInfo(userId, req)));
    }

    /** DELETE /api/users/me/payment-info/{id} — authenticated */
    @DeleteMapping("/me/payment-info/{paymentInfoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> deletePaymentInfo(
            @PathVariable Long paymentInfoId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.deletePaymentInfo(userId, paymentInfoId);
        return ResponseEntity.ok(ApiResponse.success("Deleted"));
    }
}
