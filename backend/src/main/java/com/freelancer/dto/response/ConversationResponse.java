package com.freelancer.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConversationResponse {
    private Long   id;
    private Long   contractId;
    private Long   clientId;
    private Long   freelancerId;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
}
