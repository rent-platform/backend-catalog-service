package ru.rentplatform.catalogservice.core.service;

import ru.rentplatform.catalogservice.api.dto.request.CreateCategoryRequest;
import ru.rentplatform.catalogservice.api.dto.request.UpdateCategoryRequest;
import ru.rentplatform.catalogservice.api.dto.response.CategoryResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getActiveCategories();

    CategoryResponse getCategoryById(Long categoryId);

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request);

    MessageResponse deleteCategory(Long categoryId);
}
