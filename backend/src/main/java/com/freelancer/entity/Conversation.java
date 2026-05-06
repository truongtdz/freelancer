package com.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
