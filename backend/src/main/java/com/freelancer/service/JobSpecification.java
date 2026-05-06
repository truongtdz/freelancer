package com.freelancer.service;

import com.freelancer.dto.request.JobSearchRequest;
import com.freelancer.entity.Job;
import com.freelancer.entity.JobSkill;
import com.freelancer.entity.enums.JobStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    private JobSpecification() {}

    public static Specification<Job> build(JobSearchRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), JobStatus.OPEN));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (StringUtils.hasText(req.getKeyword())) {
                String like = "%" + req.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            if (req.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), req.getCategoryId()));
            }

            if (req.getBudgetType() != null) {
                predicates.add(cb.equal(root.get("budgetType"), req.getBudgetType()));
            }

            if (req.getWorkMode() != null) {
                predicates.add(cb.equal(root.get("workMode"), req.getWorkMode()));
            }

            // Budget overlap: job.budgetMax >= req.budgetMin AND job.budgetMin <= req.budgetMax
            if (req.getBudgetMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("budgetMax"), req.getBudgetMin()));
            }
            if (req.getBudgetMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("budgetMin"), req.getBudgetMax()));
            }

            if (req.getSkillIds() != null && !req.getSkillIds().isEmpty()) {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<JobSkill> jsRoot = sub.from(JobSkill.class);
                sub.select(jsRoot.get("jobId"))
                        .where(cb.and(
                                cb.equal(jsRoot.get("jobId"), root.get("id")),
                                jsRoot.get("skillId").in(req.getSkillIds())
                        ));
                predicates.add(cb.exists(sub));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
