package ru.rentplatform.catalogservice.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.core.service.ItemStatusService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ItemStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemStatusService itemStatusService;

    @Test
    void sendToModeration_shouldReturnItem() throws Exception {
        UUID itemId = UUID.randomUUID();
        ItemResponse response = ItemResponse.builder()
                .id(itemId).title("Test").status("MODERATION").build();

        when(itemStatusService.sendToModeration(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/catalog/items/" + itemId + "/send-to-moderation")
                        .with(jwt().jwt(j -> j.claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MODERATION"));
    }
}
