package com.freelancer.repository;

import com.freelancer.entity.Category;
import com.freelancer.entity.enums.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIdIsNullAndStatus(CategoryStatus status);

    List<Category> findByParentId(Long parentId);

    Optional<Category> findBySlug(String slug);

    List<Category> findByStatusOrderByName(CategoryStatus status);

    Page<Category> findByStatus(CategoryStatus status, Pageable pageable);
}
