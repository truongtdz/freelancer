package com.freelancer.repository;

import com.freelancer.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE UserProfile p SET p.ratingAvg = :avg, p.totalReviews = p.totalReviews + 1 WHERE p.userId = :userId")
    void updateRatingAvg(@Param("userId") Long userId, @Param("avg") BigDecimal avg);

    @Modifying
    @Query("UPDATE UserProfile p SET p.totalJobsDone = p.totalJobsDone + 1 WHERE p.userId = :userId")
    void incrementTotalJobsDone(@Param("userId") Long userId);
}
