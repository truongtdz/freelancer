package com.freelancer.service.impl;

import com.freelancer.dto.response.CategoryResponse;
import com.freelancer.entity.enums.CategoryStatus;
import com.freelancer.mapper.CategoryMapper;
import com.freelancer.repository.CategoryRepository;
import com.freelancer.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findByStatusOrderByName(CategoryStatus.ACTIVE)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
