package ru.rentplatform.catalogservice.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rentplatform.catalogservice.api.dto.response.AvailabilityResponse;
import ru.rentplatform.catalogservice.core.service.AvailabilityService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    @Test
    void getAvailability_shouldReturnSlots() throws Exception {
        UUID itemId = UUID.randomUUID();
        AvailabilityResponse slot = AvailabilityResponse.builder()
                .availableDate(LocalDate.now()).isAvailable(true).build();

        when(availabilityService.getAvailability(any(), any(), any(), any()))
                .thenReturn(List.of(slot));

        mockMvc.perform(get("/api/catalog/items/" + itemId + "/availability")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusDays(7).toString()))
                .andExpect(status().isOk());
    }

    @Test
    void setAvailability_shouldSaveSlots() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(availabilityService.setAvailability(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(put("/api/catalog/items/" + itemId + "/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"slots":[{"date":"%s","isAvailable":true}]}
                        """.formatted(LocalDate.now().plusDays(1)))
                        .with(jwt().jwt(j -> j.claim("sub", "3227ee7b-775f-4743-8781-5563f352f9a7"))))
                .andExpect(status().isOk());
    }
}
