package ru.rentplatform.catalogservice.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.core.service.FavoriteService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

    @Test
    void addToFavorites_shouldReturnMessage() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(favoriteService.addToFavorites(any(), any()))
                .thenReturn(MessageResponse.builder().message("Added").build());

        mockMvc.perform(post("/api/catalog/favorites/" + itemId)
                        .with(jwt().jwt(j -> j.claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Added"));
    }

    @Test
    void getMyFavorites_shouldReturnList() throws Exception {

        ItemShortResponse item = ItemShortResponse.builder()
                .id(UUID.randomUUID()).title("Favorite").build();

        when(favoriteService.getMyFavorites(any(), any()))
                .thenReturn(new PageImpl<>(List.of(item)));

        mockMvc.perform(get("/api/catalog/favorites/my")
                        .with(jwt().jwt(j -> j.claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Favorite"));
    }

    @Test
    void isFavorite_shouldReturnTrue() throws Exception {

        UUID itemId = UUID.randomUUID();
        when(favoriteService.isFavorite(any(), any())).thenReturn(true);

        mockMvc.perform(get("/api/catalog/favorites/" + itemId + "/status")
                        .with(jwt().jwt(j -> j.claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7"))))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
