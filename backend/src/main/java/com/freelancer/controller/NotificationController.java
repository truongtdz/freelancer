package com.freelancer.controller;

import com.freelancer.dto.response.ApiResponse;
import com.freelancer.dto.response.NotificationResponse;
import com.freelancer.security.CustomUserDetails;
import com.freelancer.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getMyNotifications(principal.getId(), pageable)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails principal) {
        long count = notificationService.getUnreadCount(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        notificationService.markRead(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal CustomUserDetails principal) {
        notificationService.markAllRead(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
