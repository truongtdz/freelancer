package com.freelancer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MessageResponse {
    private Long   id;
    private Long   conversationId;
    private Long   senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String attachmentUrl;
    @JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime createdAt;
}
