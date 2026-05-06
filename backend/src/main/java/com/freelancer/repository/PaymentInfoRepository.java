package com.freelancer.repository;

import com.freelancer.entity.PaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {

    List<PaymentInfo> findByUserId(Long userId);

    Optional<PaymentInfo> findByUserIdAndIsDefaultTrue(Long userId);

    Optional<PaymentInfo> findByIdAndUserId(Long id, Long userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE PaymentInfo p SET p.isDefault = false WHERE p.userId = :userId")
    void clearDefaultForUser(@org.springframework.data.repository.query.Param("userId") Long userId);
}
