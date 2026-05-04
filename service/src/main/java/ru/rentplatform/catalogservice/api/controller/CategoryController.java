package ru.rentplatform.catalogservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.catalogservice.api.dto.response.CategoryResponse;
import ru.rentplatform.catalogservice.core.service.CategoryService;

import java.util.List;

import static ru.rentplatform.catalogservice.api.ApiPaths.CATALOG;

@RestController
@RequestMapping(CATALOG + "/categories")
@RequiredArgsConstructor
@Tag(name = "Категории", description = "Просмотр категорий товаров")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Список категорий", description = "Публичный список всех активных категорий")
    public List<CategoryResponse> getCategories() {
        return categoryService.getActiveCategories();
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Категория по ID", description = "Публичная информация о конкретной категории")
    public CategoryResponse getCategoryById(@PathVariable Long categoryId) {
        return categoryService.getCategoryById(categoryId);
    }
}
