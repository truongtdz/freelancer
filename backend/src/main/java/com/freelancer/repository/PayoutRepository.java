package com.freelancer.repository;

import com.freelancer.entity.Payout;
import com.freelancer.entity.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    Optional<Payout> findByContractId(Long contractId);

    Page<Payout> findByFreelancerId(Long freelancerId, Pageable pageable);

    Page<Payout> findByStatus(PayoutStatus status, Pageable pageable);
}
