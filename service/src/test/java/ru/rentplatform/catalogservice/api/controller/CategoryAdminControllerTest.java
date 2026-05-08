package ru.rentplatform.catalogservice.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rentplatform.catalogservice.api.dto.response.CategoryResponse;
import ru.rentplatform.catalogservice.core.service.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CategoryAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @WithMockUser(roles = "admin")
    void createCategory_shouldReturnCategory() throws Exception {

        CategoryResponse response = CategoryResponse.builder()
                .id(1L).categoryName("New Category").build();

        when(categoryService.createCategory(any())).thenReturn(response);

        mockMvc.perform(post("/api/catalog/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "categoryName": "New Category",
                    "slug": "new",
                    "sortOrder": 0,
                    "isActive": true
                }
                """))
                .andExpect(status().isOk());
    }
}
