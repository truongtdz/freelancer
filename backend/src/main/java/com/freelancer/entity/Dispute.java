package com.freelancer.entity;

import com.freelancer.entity.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "raised_by_user_id")
    private Long raisedByUserId;

    @Column(length = 255)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "resolved_by_admin_id")
    private Long resolvedByAdminId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
