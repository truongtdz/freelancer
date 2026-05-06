package com.freelancer.repository;

import com.freelancer.entity.Dispute;
import com.freelancer.entity.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);

    Optional<Dispute> findByContractId(Long contractId);

    boolean existsByContractId(Long contractId);

    long countByStatus(DisputeStatus status);
}
