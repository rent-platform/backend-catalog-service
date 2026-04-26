package ru.rentplatform.catalogservice.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDealInfoResponse {

    private UUID id;

    private UUID ownerId;

    private String status;

    private BigDecimal pricePerDay;

    private BigDecimal pricePerHour;

    private BigDecimal depositAmount;
}
