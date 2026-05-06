package com.freelancer.mapper;

import com.freelancer.dto.response.CategoryResponse;
import com.freelancer.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
    List<CategoryResponse> toResponseList(List<Category> categories);
}
