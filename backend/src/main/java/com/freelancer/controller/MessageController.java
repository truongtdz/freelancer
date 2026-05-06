package com.freelancer.controller;

import com.freelancer.dto.request.SendMessageRequest;
import com.freelancer.dto.response.ApiResponse;
import com.freelancer.dto.response.ConversationResponse;
import com.freelancer.dto.response.MessageResponse;
import com.freelancer.security.CustomUserDetails;
import com.freelancer.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** GET /api/contracts/{contractId}/conversation — lấy hoặc tạo conversation cho contract */
    @GetMapping("/contracts/{contractId}/conversation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable Long contractId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getOrCreateConversation(contractId, principal.getId())));
    }

    /** GET /api/conversations/{conversationId}/messages */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getMessages(conversationId, principal.getId(),
                        PageRequest.of(page, size))));
    }

    /** POST /api/conversations/{conversationId}/messages */
    @PostMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.sendMessage(conversationId, principal.getId(), req)));
    }
}
