package com.freelancer.entity;

import com.freelancer.entity.enums.ReviewType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(columnNames = {"contract_id", "reviewer_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewee_id")
    private Long revieweeId;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", length = 30)
    private ReviewType reviewType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
