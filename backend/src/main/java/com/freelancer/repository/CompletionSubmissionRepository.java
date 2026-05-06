package com.freelancer.repository;

import com.freelancer.entity.CompletionSubmission;
import com.freelancer.entity.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompletionSubmissionRepository extends JpaRepository<CompletionSubmission, Long> {

    List<CompletionSubmission> findByContractIdOrderByAttemptNumberDesc(Long contractId);

    Optional<CompletionSubmission> findTopByContractIdOrderByAttemptNumberDesc(Long contractId);

    Optional<CompletionSubmission> findByContractIdAndStatus(Long contractId, SubmissionStatus status);

    long countByContractId(Long contractId);

    Optional<CompletionSubmission> findTopByContractIdAndStatusOrderByAttemptNumberDesc(
            Long contractId, SubmissionStatus status);

    @Modifying
    @Query("""
            UPDATE CompletionSubmission s SET s.status = 'SUPERSEDED'
            WHERE s.contractId = :contractId AND s.status = 'REJECTED'
            """)
    void supersedeRejected(@Param("contractId") Long contractId);
}
