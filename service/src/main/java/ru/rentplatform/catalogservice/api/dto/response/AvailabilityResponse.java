package ru.rentplatform.catalogservice.api.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {

    private LocalDate availableDate;

    private Boolean isAvailable;
}