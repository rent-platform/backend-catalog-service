package ru.rentplatform.catalogservice.api.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private UUID id;

    private UUID ownerId;

    private OwnerShortResponse owner;

    private CategoryResponse category;

    private String title;

    private String itemDescription;

    private BigDecimal pricePerDay;

    private BigDecimal pricePerHour;

    private BigDecimal depositAmount;

    private String city;

    private String pickupLocation;

    private String status;

    private Boolean isFavorite;

    private String moderationComment;

    private Integer viewsCount;

    private List<PhotoResponse> photos;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}