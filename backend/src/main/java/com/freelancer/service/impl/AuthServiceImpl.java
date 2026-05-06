package com.freelancer.service.impl;

import com.freelancer.dto.request.LoginRequest;
import com.freelancer.dto.request.RegisterRequest;
import com.freelancer.dto.response.AuthResponse;
import com.freelancer.dto.response.UserSummaryResponse;
import com.freelancer.entity.User;
import com.freelancer.entity.UserProfile;
import com.freelancer.entity.enums.UserRole;
import com.freelancer.entity.enums.UserStatus;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.repository.UserProfileRepository;
import com.freelancer.repository.UserRepository;
import com.freelancer.security.CustomUserDetails;
import com.freelancer.security.JwtUtil;
import com.freelancer.service.AuthService;
import com.freelancer.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        // Reject ADMIN self-registration
        if (req.getRole() == UserRole.ADMIN) {
            throw new AppException(ErrorCode.FORBIDDEN, "Không thể tự đăng ký ADMIN");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new AppException(ErrorCode.DUPLICATE_USERNAME);
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .role(req.getRole())
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        userRepository.save(user);

        // Tạo profile rỗng
        UserProfile profile = UserProfile.builder()
                .userId(user.getId())
                .ratingAvg(BigDecimal.ZERO)
                .totalReviews(0)
                .totalJobsDone(0)
                .build();
        userProfileRepository.save(profile);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);

        log.info("User registered: {} ({})", user.getEmail(), user.getRole());
        return buildAuthResponse(token, user);
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException(ErrorCode.ACCOUNT_BANNED);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(token, user);
    }

    @Override
    public void logout(String token) {
        // Stateless JWT — FE xoá localStorage. Không cần blacklist.
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            log.info("User {} logged out at {}", userId, LocalDateTime.now());
        }
    }

    @Override
    public UserSummaryResponse getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return toSummary(user);
    }

    // ---- helpers ----

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .user(toSummary(user))
                .build();
    }

    private UserSummaryResponse toSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
