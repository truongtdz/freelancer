package com.freelancer.dto.response;

import com.freelancer.entity.enums.UserRole;
import com.freelancer.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private UserRole role;
    private UserStatus status;
    private boolean emailVerified;

    // profile
    private String bio;
    private String title;
    private Integer experienceYears;
    private BigDecimal hourlyRate;
    private BigDecimal ratingAvg;
    private int totalReviews;
    private int totalJobsDone;

    // skills (FREELANCER only)
    private List<UserSkillResponse> skills;

    private LocalDateTime createdAt;
}
