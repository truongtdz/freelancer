package com.freelancer.controller;

import com.freelancer.dto.request.CompletionRejectRequest;
import com.freelancer.dto.request.CompletionSubmitRequest;
import com.freelancer.dto.request.DisputeRequest;
import com.freelancer.dto.request.ProgressReportRequest;
import com.freelancer.dto.response.*;
import com.freelancer.entity.enums.ContractStatus;
import com.freelancer.security.CustomUserDetails;
import com.freelancer.service.ContractService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    // ── GET /api/contracts ────────────────────────────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ContractListItemResponse>>> getMyContracts(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(required = false)     ContractStatus status,
            @AuthenticationPrincipal CustomUserDetails principal) {

        String role = extractRole(principal);
        Page<ContractListItemResponse> data = contractService.getMyContracts(
                principal.getId(), role, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ── GET /api/contracts/{id} ───────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> getContractById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ContractDetailResponse detail = contractService.getContractById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    // ── POST /api/contracts/{id}/pay ─────────────────────────────
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Map<String, String>>> initiatePayment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            HttpServletRequest request) {

        String paymentUrl = contractService.initiatePayment(id, principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("paymentUrl", paymentUrl)));
    }

    // ── POST /api/contracts/{id}/progress ────────────────────────
    @PostMapping("/{id}/progress")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ProgressReportResponse>> createProgressReport(
            @PathVariable Long id,
            @Valid @RequestBody ProgressReportRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ProgressReportResponse result = contractService.createProgressReport(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── POST /api/contracts/{id}/submit-completion ───────────────
    @PostMapping("/{id}/submit-completion")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> submitCompletion(
            @PathVariable Long id,
            @Valid @RequestBody CompletionSubmitRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ContractDetailResponse result = contractService.submitCompletion(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── POST /api/contracts/{id}/confirm-completion ──────────────
    @PostMapping("/{id}/confirm-completion")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> confirmCompletion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ContractDetailResponse result = contractService.confirmCompletion(id, principal.getId(), false);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── POST /api/contracts/{id}/reject-completion ───────────────
    @PostMapping("/{id}/reject-completion")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> rejectCompletion(
            @PathVariable Long id,
            @Valid @RequestBody CompletionRejectRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ContractDetailResponse result = contractService.rejectCompletion(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── POST /api/contracts/{id}/dispute ─────────────────────────
    @PostMapping("/{id}/dispute")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ContractDetailResponse>> raiseDispute(
            @PathVariable Long id,
            @Valid @RequestBody DisputeRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ContractDetailResponse result = contractService.raiseDispute(id, req, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ─── helper ───────────────────────────────────────────────────

    private String extractRole(CustomUserDetails principal) {
        return principal.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .filter(r -> r.equals("CLIENT") || r.equals("FREELANCER"))
                .findFirst().orElse("FREELANCER");
    }
}
