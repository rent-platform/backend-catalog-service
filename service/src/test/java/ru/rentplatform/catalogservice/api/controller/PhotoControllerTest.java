package ru.rentplatform.catalogservice.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rentplatform.catalogservice.api.dto.response.PhotoResponse;
import ru.rentplatform.catalogservice.core.service.PhotoService;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class PhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PhotoService photoService;

    @Test
    void getItemPhotos_shouldReturnList() throws Exception {

        UUID itemId = UUID.randomUUID();
        PhotoResponse photo = PhotoResponse.builder()
                .id(UUID.randomUUID()).photoUrl("https://example.com/1.jpg").build();

        when(photoService.getItemPhotos(itemId)).thenReturn(List.of(photo));

        mockMvc.perform(get("/api/catalog/items/" + itemId + "/photos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].photoUrl").value("https://example.com/1.jpg"));
    }
}
