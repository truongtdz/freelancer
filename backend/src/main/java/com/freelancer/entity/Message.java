package com.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
