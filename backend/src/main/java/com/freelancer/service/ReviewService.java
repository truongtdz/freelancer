package com.freelancer.service;

import com.freelancer.dto.request.ReviewCreateRequest;
import com.freelancer.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse createReview(Long contractId, ReviewCreateRequest req, Long reviewerId);

    Page<ReviewResponse> getReviewsByUser(Long userId, Pageable pageable);
}
