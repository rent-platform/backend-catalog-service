package ru.rentplatform.catalogservice.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.core.service.CatalogService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogService catalogService;

    @Test
    void getActiveItems_shouldReturnList() throws Exception {
        ItemShortResponse item = ItemShortResponse.builder()
                .id(UUID.randomUUID()).title("Test").status("ACTIVE").build();

        when(catalogService.getActiveItems(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(item)));

        mockMvc.perform(get("/api/catalog/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test"));
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        UUID itemId = UUID.randomUUID();
        ItemResponse response = ItemResponse.builder()
                .id(itemId).title("Test Item").status("ACTIVE").build();

        when(catalogService.getItemById(any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/catalog/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Item"));
    }
}
