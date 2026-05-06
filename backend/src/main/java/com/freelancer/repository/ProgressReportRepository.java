package com.freelancer.repository;

import com.freelancer.entity.ProgressReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressReportRepository extends JpaRepository<ProgressReport, Long> {

    List<ProgressReport> findByContractIdOrderByReportedAtDesc(Long contractId);

    Optional<ProgressReport> findTopByContractIdOrderByProgressPercentageDesc(Long contractId);
}
