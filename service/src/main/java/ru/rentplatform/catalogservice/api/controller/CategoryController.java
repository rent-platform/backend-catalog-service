package ru.rentplatform.catalogservice.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.catalogservice.api.dto.response.CategoryResponse;
import ru.rentplatform.catalogservice.core.service.CategoryService;

import java.util.List;

import static ru.rentplatform.catalogservice.api.ApiPaths.CATALOG;

@RestController
@RequestMapping(CATALOG + "/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> getCategories() {
        return categoryService.getActiveCategories();
    }

    @GetMapping("/{categoryId}")
    public CategoryResponse getCategoryById(@PathVariable Long categoryId) {
        return categoryService.getCategoryById(categoryId);
    }
}
