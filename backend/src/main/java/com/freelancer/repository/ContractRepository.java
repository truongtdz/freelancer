package com.freelancer.repository;

import com.freelancer.entity.Contract;
import com.freelancer.entity.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Page<Contract> findByClientIdAndDeletedAtIsNull(Long clientId, Pageable pageable);

    Page<Contract> findByClientIdAndStatusAndDeletedAtIsNull(Long clientId, ContractStatus status, Pageable pageable);

    Page<Contract> findByFreelancerIdAndDeletedAtIsNull(Long freelancerId, Pageable pageable);

    Page<Contract> findByFreelancerIdAndStatusAndDeletedAtIsNull(Long freelancerId, ContractStatus status, Pageable pageable);

    Optional<Contract> findByJobId(Long jobId);

    Optional<Contract> findByIdAndDeletedAtIsNull(Long id);

    Optional<Contract> findByContractCode(String code);

    List<Contract> findByStatus(ContractStatus status);

    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);

    long countByStatus(ContractStatus status);

    boolean existsByApplicationId(Long applicationId);

    Page<Contract> findByDeletedAtIsNull(Pageable pageable);

    Page<Contract> findByStatusAndDeletedAtIsNull(ContractStatus status, Pageable pageable);

    @Query("""
            SELECT c FROM Contract c
            WHERE c.status = 'PENDING_PAYMENT'
              AND c.createdAt < :cutoff AND c.deletedAt IS NULL
            """)
    List<Contract> findExpiredPendingPayment(@Param("cutoff") LocalDateTime cutoff);

    @Query("""
            SELECT c FROM Contract c
            WHERE c.status = 'FREELANCER_SUBMITTED'
              AND c.updatedAt < :cutoff AND c.deletedAt IS NULL
            """)
    List<Contract> findAutoConfirmable(@Param("cutoff") LocalDateTime cutoff);
}
