package com.freelancer.controller;

import com.freelancer.dto.request.LoginRequest;
import com.freelancer.dto.request.RegisterRequest;
import com.freelancer.dto.response.ApiResponse;
import com.freelancer.dto.response.AuthResponse;
import com.freelancer.dto.response.UserSummaryResponse;
import com.freelancer.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request)));
    }

    // Rate-limited 5/min/IP via RateLimitFilter
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser()));
    }

    // Phase 2 — stub endpoints
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail() {
        return ResponseEntity.status(501).body(ApiResponse.error(501, "Chức năng sẽ ra mắt ở Phase 2"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword() {
        return ResponseEntity.status(501).body(ApiResponse.error(501, "Chức năng sẽ ra mắt ở Phase 2"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword() {
        return ResponseEntity.status(501).body(ApiResponse.error(501, "Chức năng sẽ ra mắt ở Phase 2"));
    }
}
