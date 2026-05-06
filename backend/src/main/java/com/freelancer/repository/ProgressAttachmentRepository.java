package com.freelancer.repository;

import com.freelancer.entity.ProgressAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressAttachmentRepository extends JpaRepository<ProgressAttachment, Long> {

    List<ProgressAttachment> findByProgressReportId(Long progressReportId);

    List<ProgressAttachment> findByProgressReportIdIn(List<Long> reportIds);
}
