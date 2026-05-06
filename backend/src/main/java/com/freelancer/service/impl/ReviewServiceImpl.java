package com.freelancer.service.impl;

import com.freelancer.dto.request.ReviewCreateRequest;
import com.freelancer.dto.response.ReviewResponse;
import com.freelancer.dto.response.UserSummaryResponse;
import com.freelancer.entity.Review;
import com.freelancer.entity.User;
import com.freelancer.entity.enums.ContractStatus;
import com.freelancer.entity.enums.ReviewType;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.repository.ContractRepository;
import com.freelancer.repository.ReviewRepository;
import com.freelancer.repository.UserProfileRepository;
import com.freelancer.repository.UserRepository;
import com.freelancer.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(Long contractId, ReviewCreateRequest req, Long reviewerId) {
        var c = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        // Contract must be PAID_OUT (or CLIENT_CONFIRMED as alternative)
        if (c.getStatus() != ContractStatus.PAID_OUT && c.getStatus() != ContractStatus.CLIENT_CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Hợp đồng chưa hoàn tất — không thể review");
        }

        // Determine roles
        Long revieweeId;
        ReviewType reviewType;
        if (reviewerId.equals(c.getClientId())) {
            revieweeId = c.getFreelancerId();
            reviewType = ReviewType.CLIENT_TO_FREELANCER;
        } else if (reviewerId.equals(c.getFreelancerId())) {
            revieweeId = c.getClientId();
            reviewType = ReviewType.FREELANCER_TO_CLIENT;
        } else {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // Idempotency
        if (reviewRepository.existsByContractIdAndReviewerId(contractId, reviewerId)) {
            throw new AppException(ErrorCode.DUPLICATE_REVIEW);
        }

        // Save review
        Review review = reviewRepository.save(Review.builder()
                .contractId(contractId)
                .reviewerId(reviewerId)
                .revieweeId(revieweeId)
                .rating(req.getRating())
                .comment(req.getComment())
                .reviewType(reviewType)
                .createdAt(LocalDateTime.now())
                .build());

        // Recalculate rating average for reviewee
        reviewRepository.calculateAverageRating(revieweeId).ifPresent(avg ->
                userProfileRepository.updateRatingAvg(revieweeId, BigDecimal.valueOf(avg))
        );

        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByRevieweeId(userId, pageable)
                .map(this::toResponse);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private ReviewResponse toResponse(Review r) {
        User reviewer = userRepository.findById(r.getReviewerId()).orElse(null);
        User reviewee = userRepository.findById(r.getRevieweeId()).orElse(null);
        return ReviewResponse.builder()
                .id(r.getId())
                .contractId(r.getContractId())
                .reviewer(toSummary(reviewer))
                .reviewee(toSummary(reviewee))
                .rating(r.getRating())
                .comment(r.getComment())
                .reviewType(r.getReviewType())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private UserSummaryResponse toSummary(User u) {
        if (u == null) return null;
        return UserSummaryResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .avatarUrl(u.getAvatarUrl())
                .role(u.getRole())
                .status(u.getStatus())
                .build();
    }
}
