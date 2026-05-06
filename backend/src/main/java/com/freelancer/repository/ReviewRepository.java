package com.freelancer.repository;

import com.freelancer.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    Page<Review> findByRevieweeId(Long revieweeId, Pageable pageable);

    boolean existsByContractIdAndReviewerId(Long contractId, Long reviewerId);

    List<Review> findByContractId(Long contractId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeId = :userId")
    Optional<Double> calculateAverageRating(@Param("userId") Long userId);
}
