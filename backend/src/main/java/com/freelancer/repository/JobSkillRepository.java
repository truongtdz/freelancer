package com.freelancer.repository;

import com.freelancer.entity.JobSkill;
import com.freelancer.entity.JobSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkillId> {

    List<JobSkill> findByJobId(Long jobId);

    void deleteByJobId(Long jobId);

    List<JobSkill> findByJobIdIn(List<Long> jobIds);
}
