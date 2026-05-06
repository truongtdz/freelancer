package com.freelancer.util;

import com.freelancer.entity.enums.UserRole;
import com.freelancer.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        return principal instanceof CustomUserDetails u ? u : null;
    }

    public static Long getCurrentUserId() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static UserRole getCurrentUserRole() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    public static boolean isAdmin() {
        return getCurrentUserRole() == UserRole.ADMIN;
    }

    public static boolean isCurrentUser(Long userId) {
        return userId != null && userId.equals(getCurrentUserId());
    }
}
