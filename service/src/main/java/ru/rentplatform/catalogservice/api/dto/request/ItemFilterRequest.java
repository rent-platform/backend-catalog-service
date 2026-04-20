package ru.rentplatform.catalogservice.api.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemFilterRequest {

    private Long categoryId;

    private String city;

    private BigDecimal minPricePerDay;

    private BigDecimal maxPricePerDay;

    private BigDecimal minPricePerHour;

    private BigDecimal maxPricePerHour;

    private String query;
}
