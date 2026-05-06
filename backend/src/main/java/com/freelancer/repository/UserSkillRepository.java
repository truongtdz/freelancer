package com.freelancer.repository;

import com.freelancer.entity.UserSkill;
import com.freelancer.entity.UserSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkillId> {

    List<UserSkill> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
