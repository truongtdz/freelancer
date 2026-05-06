package com.freelancer.controller;

import com.freelancer.dto.request.DisputeResolveRequest;
import com.freelancer.dto.request.PayoutCreateRequest;
import com.freelancer.dto.response.AdminUserResponse;
import com.freelancer.dto.response.ApiResponse;
import com.freelancer.dto.response.ContractResponse;
import com.freelancer.dto.response.DashboardStatsResponse;
import com.freelancer.dto.response.DisputeResponse;
import com.freelancer.dto.response.PayoutResponse;
import com.freelancer.dto.response.PendingPayoutItem;
import com.freelancer.dto.response.UserSummaryResponse;
import com.freelancer.entity.Contract;
import com.freelancer.entity.Job;
import com.freelancer.entity.User;
import com.freelancer.entity.enums.ContractStatus;
import com.freelancer.entity.enums.DisputeStatus;
import com.freelancer.entity.enums.UserRole;
import com.freelancer.entity.enums.UserStatus;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.repository.ContractRepository;
import com.freelancer.repository.JobRepository;
import com.freelancer.repository.UserRepository;
import com.freelancer.service.AdminDashboardService;
import com.freelancer.service.AdminDisputeService;
import com.freelancer.service.AdminPayoutService;
import com.freelancer.service.SystemSettingService;
import com.freelancer.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminPayoutService adminPayoutService;
    private final AdminDisputeService adminDisputeService;
    private final AdminDashboardService adminDashboardService;
    private final SystemSettingService systemSettingService;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final JobRepository jobRepository;

    // ── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(adminDashboardService.getStats()));
    }

    // ── Users ────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = (role != null && !role.isBlank())
                ? userRepository.findByRoleAndDeletedAtIsNull(UserRole.valueOf(role), pageable)
                : userRepository.findByDeletedAtIsNull(pageable);
        return ResponseEntity.ok(ApiResponse.success(users.map(this::toAdminUserResponse)));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.valueOf(body.get("status")));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(toAdminUserResponse(user)));
    }

    private AdminUserResponse toAdminUserResponse(User u) {
        return AdminUserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole().name())
                .status(u.getStatus().name())
                .createdAt(u.getCreatedAt())
                .build();
    }

    // ── Settings ─────────────────────────────────────────────────────────────

    @PutMapping("/settings/{key}")
    public ResponseEntity<ApiResponse<String>> updateSetting(
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        String value = body.get("value");
        Long adminId = SecurityUtils.getCurrentUserId();
        systemSettingService.update(key, value, adminId);
        return ResponseEntity.ok(ApiResponse.success("Setting updated"));
    }

    // ── Payouts ──────────────────────────────────────────────────────────────

    @GetMapping("/payouts/pending")
    public ResponseEntity<ApiResponse<Page<PendingPayoutItem>>> getPendingPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        return ResponseEntity.ok(ApiResponse.success(adminPayoutService.getPendingPayouts(pageable)));
    }

    @PostMapping("/payouts/{contractId}/pay")
    public ResponseEntity<ApiResponse<PayoutResponse>> payout(
            @PathVariable Long contractId,
            @Valid @RequestBody PayoutCreateRequest req) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(adminPayoutService.payout(contractId, req, adminId)));
    }

    // ── Disputes ─────────────────────────────────────────────────────────────

    @GetMapping("/disputes")
    public ResponseEntity<ApiResponse<Page<DisputeResponse>>> getDisputes(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(adminDisputeService.getDisputes(status, pageable)));
    }

    @PutMapping("/disputes/{id}/resolve")
    public ResponseEntity<ApiResponse<DisputeResponse>> resolveDispute(
            @PathVariable Long id,
            @Valid @RequestBody DisputeResolveRequest req) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(adminDisputeService.resolveDispute(id, req, adminId)));
    }

    // ── Contracts ────────────────────────────────────────────────────────────

    @GetMapping("/contracts")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getContracts(
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Contract> contracts = (status != null)
                ? contractRepository.findByStatusAndDeletedAtIsNull(status, pageable)
                : contractRepository.findByDeletedAtIsNull(pageable);
        return ResponseEntity.ok(ApiResponse.success(contracts.map(this::toContractResponse)));
    }

    @GetMapping("/contracts/{id}")
    public ResponseEntity<ApiResponse<ContractResponse>> getContract(@PathVariable Long id) {
        Contract contract = contractRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(toContractResponse(contract)));
    }

    private ContractResponse toContractResponse(Contract c) {
        User client     = userRepository.findById(c.getClientId()).orElse(null);
        User freelancer = userRepository.findById(c.getFreelancerId()).orElse(null);
        Job  job        = jobRepository.findById(c.getJobId()).orElse(null);
        return ContractResponse.builder()
                .id(c.getId())
                .contractCode(c.getContractCode())
                .jobId(c.getJobId())
                .jobTitle(job != null ? job.getTitle() : null)
                .client(toUserSummary(client))
                .freelancer(toUserSummary(freelancer))
                .agreedPrice(c.getAgreedPrice())
                .commissionRate(c.getCommissionRate())
                .commissionAmount(c.getCommissionAmount())
                .netAmount(c.getNetAmount())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private UserSummaryResponse toUserSummary(User u) {
        if (u == null) return null;
        return UserSummaryResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .avatarUrl(u.getAvatarUrl())
                .role(u.getRole())
                .status(u.getStatus())
                .build();
    }
}
