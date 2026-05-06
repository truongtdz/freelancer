package com.freelancer.service.impl;

import com.freelancer.dto.request.PaymentInfoRequest;
import com.freelancer.dto.request.UserProfileUpdateRequest;
import com.freelancer.dto.response.PaymentInfoResponse;
import com.freelancer.dto.response.UserProfileResponse;
import com.freelancer.dto.response.UserSkillResponse;
import com.freelancer.entity.*;
import com.freelancer.entity.enums.UserRole;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.repository.*;
import com.freelancer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final PaymentInfoRepository paymentInfoRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);

        List<UserSkillResponse> skills = List.of();
        if (user.getRole() == UserRole.FREELANCER) {
            skills = buildSkillResponses(userId);
        }

        return toProfileResponse(user, profile, skills);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(Long userId, UserProfileUpdateRequest req) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Update User fields
        if (StringUtils.hasText(req.getFullName())) user.setFullName(req.getFullName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());
        user = userRepository.save(user);

        // Upsert UserProfile
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().userId(userId).build());
        if (req.getBio() != null) profile.setBio(req.getBio());
        if (StringUtils.hasText(req.getTitle())) profile.setTitle(req.getTitle());
        if (req.getExperienceYears() != null) profile.setExperienceYears(req.getExperienceYears());
        if (req.getHourlyRate() != null) profile.setHourlyRate(req.getHourlyRate());
        profile = userProfileRepository.save(profile);

        // Update skills (FREELANCER only)
        if (user.getRole() == UserRole.FREELANCER && req.getSkills() != null) {
            userSkillRepository.deleteByUserId(userId);
            List<UserSkill> newSkills = req.getSkills().stream()
                    .map(s -> UserSkill.builder()
                            .userId(userId)
                            .skillId(s.getSkillId())
                            .level(s.getLevel())
                            .build())
                    .collect(Collectors.toList());
            userSkillRepository.saveAll(newSkills);
        }

        List<UserSkillResponse> skillResponses = user.getRole() == UserRole.FREELANCER
                ? buildSkillResponses(userId)
                : List.of();

        return toProfileResponse(user, profile, skillResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentInfoResponse> getMyPaymentInfos(Long userId) {
        return paymentInfoRepository.findByUserId(userId).stream()
                .map(this::toPaymentInfoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentInfoResponse savePaymentInfo(Long userId, PaymentInfoRequest req) {
        // Clear existing default if this one is default
        if (req.isDefault()) {
            paymentInfoRepository.clearDefaultForUser(userId);
        }

        PaymentInfo pi = paymentInfoRepository.save(PaymentInfo.builder()
                .userId(userId)
                .bankName(req.getBankName())
                .bankAccountNumber(req.getBankAccountNumber())
                .bankAccountHolder(req.getBankAccountHolder())
                .qrCodeUrl(req.getQrCodeUrl())
                .isDefault(req.isDefault())
                .build());

        return toPaymentInfoResponse(pi);
    }

    @Override
    @Transactional
    public void deletePaymentInfo(Long userId, Long paymentInfoId) {
        PaymentInfo pi = paymentInfoRepository.findByIdAndUserId(paymentInfoId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND, "Payment info not found"));
        paymentInfoRepository.delete(pi);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private List<UserSkillResponse> buildSkillResponses(Long userId) {
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        if (userSkills.isEmpty()) return List.of();

        List<Long> skillIds = userSkills.stream().map(UserSkill::getSkillId).toList();
        Map<Long, Skill> skillMap = skillRepository.findByIdIn(skillIds).stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));

        return userSkills.stream().map(us -> {
            Skill skill = skillMap.get(us.getSkillId());
            return UserSkillResponse.builder()
                    .skillId(us.getSkillId())
                    .name(skill != null ? skill.getName() : "")
                    .slug(skill != null ? skill.getSlug() : "")
                    .level(us.getLevel())
                    .build();
        }).collect(Collectors.toList());
    }

    private UserProfileResponse toProfileResponse(User user, UserProfile profile,
                                                    List<UserSkillResponse> skills) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .bio(profile != null ? profile.getBio() : null)
                .title(profile != null ? profile.getTitle() : null)
                .experienceYears(profile != null ? profile.getExperienceYears() : null)
                .hourlyRate(profile != null ? profile.getHourlyRate() : null)
                .ratingAvg(profile != null ? profile.getRatingAvg() : null)
                .totalReviews(profile != null ? profile.getTotalReviews() : 0)
                .totalJobsDone(profile != null ? profile.getTotalJobsDone() : 0)
                .skills(skills)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private PaymentInfoResponse toPaymentInfoResponse(PaymentInfo pi) {
        // Mask account number: show only last 4 digits
        String masked = maskAccount(pi.getBankAccountNumber());
        return PaymentInfoResponse.builder()
                .id(pi.getId())
                .bankName(pi.getBankName())
                .bankAccountNumber(masked)
                .bankAccountHolder(pi.getBankAccountHolder())
                .qrCodeUrl(pi.getQrCodeUrl())
                .isDefault(pi.isDefault())
                .build();
    }

    private String maskAccount(String account) {
        if (account == null || account.length() <= 4) return account;
        return "****" + account.substring(account.length() - 4);
    }
}
