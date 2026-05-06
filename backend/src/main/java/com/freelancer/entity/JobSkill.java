package com.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_skills")
@IdClass(JobSkillId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSkill {

    @Id
    @Column(name = "job_id")
    private Long jobId;

    @Id
    @Column(name = "skill_id")
    private Long skillId;
}
