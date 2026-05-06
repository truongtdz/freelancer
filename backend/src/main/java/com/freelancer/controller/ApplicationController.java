package com.freelancer.controller;

import com.freelancer.dto.request.ApplicationCreateRequest;
import com.freelancer.dto.request.RejectApplicationRequest;
import com.freelancer.dto.response.ApiResponse;
import com.freelancer.dto.response.ApplicationListItemResponse;
import com.freelancer.dto.response.ApplicationResponse;
import com.freelancer.dto.response.ContractResponse;
import com.freelancer.entity.enums.ApplicationStatus;
import com.freelancer.service.ApplicationService;
import com.freelancer.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // -------------------------------------------------------------------------
    // POST /api/jobs/{jobId}/apply  [FREELANCER]
    // -------------------------------------------------------------------------

    @PostMapping("/api/jobs/{jobId}/apply")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
            @PathVariable Long jobId,
            @Valid @RequestBody ApplicationCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long freelancerId = extractUserId(principal);
        ApplicationResponse result = applicationService.apply(jobId, req, freelancerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/applications/{id}  [FREELANCER - own withdraw]
    // -------------------------------------------------------------------------

    @DeleteMapping("/api/applications/{id}")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        applicationService.withdraw(id, extractUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // -------------------------------------------------------------------------
    // GET /api/jobs/{jobId}/applications  [CLIENT - owner]
    // -------------------------------------------------------------------------

    @GetMapping("/api/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<ApplicationListItemResponse>>> getByJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 10, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ApplicationListItemResponse> page =
                applicationService.getApplicationsByJob(jobId, extractUserId(principal), pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    // -------------------------------------------------------------------------
    // GET /api/my/applications  [FREELANCER]
    // -------------------------------------------------------------------------

    @GetMapping("/api/my/applications")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getMyApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 10, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ApplicationResponse> page =
                applicationService.getMyApplications(extractUserId(principal), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    // -------------------------------------------------------------------------
    // PUT /api/applications/{id}/accept  [CLIENT]
    // -------------------------------------------------------------------------

    @PutMapping("/api/applications/{id}/accept")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ContractResponse>> accept(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ContractResponse result = applicationService.acceptApplication(id, extractUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // -------------------------------------------------------------------------
    // PUT /api/applications/{id}/reject  [CLIENT]
    // -------------------------------------------------------------------------

    @PutMapping("/api/applications/{id}/reject")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) RejectApplicationRequest body,
            @AuthenticationPrincipal CustomUserDetails principal) {

        String reason = body != null ? body.getReason() : null;
        ApplicationResponse result =
                applicationService.rejectApplication(id, reason, extractUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Long extractUserId(CustomUserDetails principal) {
        return principal.getId();
    }
}
