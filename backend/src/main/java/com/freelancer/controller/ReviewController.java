package com.freelancer.controller;

import com.freelancer.dto.request.ReviewCreateRequest;
import com.freelancer.dto.response.ApiResponse;
import com.freelancer.dto.response.ReviewResponse;
import com.freelancer.service.ReviewService;
import com.freelancer.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** POST /api/contracts/{id}/review — authenticated participant */
    @PostMapping("/api/contracts/{id}/review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewCreateRequest req) {
        Long reviewerId = SecurityUtils.getCurrentUserId();
        ReviewResponse response = reviewService.createReview(id, req, reviewerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** GET /api/users/{id}/reviews — public */
    @GetMapping("/api/users/{id}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByUser(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsByUser(id, pageable)));
    }
}
