package com.freelancer.repository;

import com.freelancer.entity.JobAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobAttachmentRepository extends JpaRepository<JobAttachment, Long> {

    List<JobAttachment> findByJobId(Long jobId);

    void deleteByJobId(Long jobId);
}
