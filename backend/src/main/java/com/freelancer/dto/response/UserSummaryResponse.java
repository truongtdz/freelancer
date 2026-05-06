package com.freelancer.dto.response;

import com.freelancer.entity.enums.UserRole;
import com.freelancer.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private UserRole role;
    private UserStatus status;
}
