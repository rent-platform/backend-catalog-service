package ru.rentplatform.catalogservice.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemShortResponse {

    private UUID id;

    private String title;

    private String city;

    private BigDecimal pricePerDay;

    private BigDecimal pricePerHour;

    private String status;

    private Boolean isFavorite;

    private String mainPhotoUrl;
}