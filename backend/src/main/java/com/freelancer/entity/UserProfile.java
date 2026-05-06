package com.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 200)
    private String title;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "hourly_rate", precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String linkedin;

    @Column(length = 255)
    private String github;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private int totalReviews = 0;

    @Column(name = "total_jobs_done")
    @Builder.Default
    private int totalJobsDone = 0;
}
