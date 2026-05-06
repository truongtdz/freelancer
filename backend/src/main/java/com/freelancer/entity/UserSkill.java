package com.freelancer.entity;

import com.freelancer.entity.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_skills")
@IdClass(UserSkillId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkill {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "skill_id")
    private Long skillId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SkillLevel level;
}
