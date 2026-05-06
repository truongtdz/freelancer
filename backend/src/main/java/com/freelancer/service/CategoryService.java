package com.freelancer.service;

import com.freelancer.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAll();
}
