package ru.rentplatform.catalogservice.core.service.implement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rentplatform.catalogservice.api.dto.request.CreateCategoryRequest;
import ru.rentplatform.catalogservice.api.dto.response.CategoryResponse;
import ru.rentplatform.catalogservice.core.dao.entity.Category;
import ru.rentplatform.catalogservice.core.dao.repository.CategoryRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void getActiveCategories_shouldReturnList() {

        Category category = new Category();
        category.setId(1L);
        category.setCategoryName("Test");

        when(categoryRepository.findAllByDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc())
                .thenReturn(List.of(category));
        when(catalogMapper.toCategoryResponse(category)).thenReturn(new CategoryResponse());

        List<CategoryResponse> result = categoryService.getActiveCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void createCategory_shouldCreate() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .categoryName("New").slug("new").sortOrder(0).isActive(true).build();

        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(catalogMapper.toCategoryResponse(any(Category.class))).thenReturn(new CategoryResponse());

        CategoryResponse result = categoryService.createCategory(request);

        assertNotNull(result);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void getCategoryById_shouldReturnCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setCategoryName("Test");

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(catalogMapper.toCategoryResponse(category)).thenReturn(new CategoryResponse());

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertNotNull(result);
    }
}
