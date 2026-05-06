package com.freelancer.repository;

import com.freelancer.entity.JobApplication;
import com.freelancer.entity.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);

    List<JobApplication> findByFreelancerIdOrderByAppliedAtDesc(Long freelancerId);

    Page<JobApplication> findByFreelancerId(Long freelancerId, Pageable pageable);

    Page<JobApplication> findByFreelancerIdAndStatus(Long freelancerId, ApplicationStatus status, Pageable pageable);

    Optional<JobApplication> findByJobIdAndFreelancerId(Long jobId, Long freelancerId);

    boolean existsByJobIdAndFreelancerId(Long jobId, Long freelancerId);

    List<JobApplication> findByJobIdAndStatus(Long jobId, ApplicationStatus status);

    long countByJobIdAndStatus(Long jobId, ApplicationStatus status);

    @Query("SELECT a.jobId, COUNT(a) FROM JobApplication a WHERE a.jobId IN :jobIds AND a.status = 'PENDING' GROUP BY a.jobId")
    List<Object[]> countPendingByJobIdIn(@Param("jobIds") List<Long> jobIds);

    @Modifying
    @Query("""
            UPDATE JobApplication a SET a.status = 'REJECTED', a.respondedAt = CURRENT_TIMESTAMP
            WHERE a.jobId = :jobId AND a.id <> :acceptedId AND a.status = 'PENDING'
            """)
    void rejectOtherApplications(@Param("jobId") Long jobId, @Param("acceptedId") Long acceptedId);
}
