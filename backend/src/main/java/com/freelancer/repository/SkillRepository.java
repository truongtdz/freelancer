package com.freelancer.repository;

import com.freelancer.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findBySlug(String slug);

    List<Skill> findByNameContainingIgnoreCase(String keyword);

    List<Skill> findByIdIn(List<Long> ids);
}
