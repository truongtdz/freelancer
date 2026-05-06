package com.freelancer.repository;

import com.freelancer.entity.Job;
import com.freelancer.entity.enums.JobStatus;
import com.freelancer.entity.enums.WorkMode;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Page<Job> findByStatusAndDeletedAtIsNull(JobStatus status, Pageable pageable);

    Page<Job> findByStatusAndCategoryIdAndDeletedAtIsNull(JobStatus status, Long categoryId, Pageable pageable);

    @Query("""
            SELECT j FROM Job j
            WHERE j.deletedAt IS NULL AND j.status = :status
              AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR j.categoryId = :categoryId)
              AND (:workMode IS NULL OR j.workMode = :workMode)
            """)
    Page<Job> searchJobs(@Param("status") JobStatus status,
                         @Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("workMode") WorkMode workMode,
                         Pageable pageable);

    Page<Job> findByClientIdAndDeletedAtIsNull(Long clientId, Pageable pageable);

    Page<Job> findByClientIdAndStatusAndDeletedAtIsNull(Long clientId, JobStatus status, Pageable pageable);

    Optional<Job> findByIdAndDeletedAtIsNull(Long id);

    @Modifying
    @Query("UPDATE Job j SET j.viewCount = j.viewCount + 1 WHERE j.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Job j SET j.applicationCount = j.applicationCount + 1 WHERE j.id = :id")
    void incrementApplicationCount(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.id = :id")
    Optional<Job> findByIdWithLock(@Param("id") Long id);

    /** Pessimistic write lock — dùng khi accept application để tránh race condition */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.id = :id AND j.deletedAt IS NULL")
    Optional<Job> findByIdForUpdate(@Param("id") Long id);

    long countByDeletedAtIsNull();

    long countByStatusAndDeletedAtIsNull(JobStatus status);
}
