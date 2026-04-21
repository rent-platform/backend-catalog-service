package ru.rentplatform.catalogservice.core.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.catalogservice.api.dto.request.CreateCategoryRequest;
import ru.rentplatform.catalogservice.api.dto.request.UpdateCategoryRequest;
import ru.rentplatform.catalogservice.api.dto.response.CategoryResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.api.exception.CategoryAlreadyExistsException;
import ru.rentplatform.catalogservice.api.exception.CategoryNotFoundException;
import ru.rentplatform.catalogservice.core.dao.entity.Category;
import ru.rentplatform.catalogservice.core.dao.repository.CategoryRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;
import ru.rentplatform.catalogservice.core.service.CategoryService;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(catalogMapper::toCategoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        return catalogMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsBySlugAndDeletedAtIsNull(request.getSlug())) {
            throw new CategoryAlreadyExistsException("Category with this slug already exists");
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findByIdAndDeletedAtIsNull(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Parent category not found"));
        }

        OffsetDateTime now = OffsetDateTime.now();

        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .slug(request.getSlug())
                .parent(parent)
                .sortOrder(request.getSortOrder())
                .isActive(request.getIsActive())
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return catalogMapper.toCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        if (request.getSlug() != null) {
            boolean slugBusy = categoryRepository.existsBySlugAndDeletedAtIsNullAndIdNot(request.getSlug(), categoryId);
            if (slugBusy) {
                throw new CategoryAlreadyExistsException("Category with this slug already exists");
            }
            category.setSlug(request.getSlug());
        }

        if (request.getCategoryName() != null) {
            category.setCategoryName(request.getCategoryName());
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(categoryId)) {
                throw new IllegalArgumentException("Category cannot be parent of itself");
            }

            Category parent = categoryRepository.findByIdAndDeletedAtIsNull(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Parent category not found"));

            category.setParent(parent);
        }

        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        category.setUpdatedAt(OffsetDateTime.now());

        Category savedCategory = categoryRepository.save(category);
        return catalogMapper.toCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public MessageResponse deleteCategory(Long categoryId) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        category.setDeletedAt(OffsetDateTime.now());
        category.setUpdatedAt(OffsetDateTime.now());
        categoryRepository.save(category);

        return MessageResponse.builder()
                .message("Category deleted successfully")
                .build();
    }
}
