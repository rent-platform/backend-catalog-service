package ru.rentplatform.catalogservice.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.catalogservice.api.dto.request.CreateCategoryRequest;
import ru.rentplatform.catalogservice.api.dto.request.UpdateCategoryRequest;
import ru.rentplatform.catalogservice.api.dto.response.CategoryResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.core.service.CategoryService;

import static ru.rentplatform.catalogservice.api.ApiPaths.CATALOG;

@RestController
@RequestMapping(CATALOG + "/admin/categories")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
public class CategoryAdminController {

    private final CategoryService categoryService;

    @PostMapping
    public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse updateCategory(@PathVariable Long categoryId,
                                           @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.updateCategory(categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    public MessageResponse deleteCategory(@PathVariable Long categoryId) {
        return categoryService.deleteCategory(categoryId);
    }
}
