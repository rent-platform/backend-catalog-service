package ru.rentplatform.catalogservice.api.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {

    private Long categoryId;

    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String itemDescription;

    @DecimalMin(value = "0.00", inclusive = true)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal pricePerDay;

    @DecimalMin(value = "0.00", inclusive = true)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal pricePerHour;

    @DecimalMin(value = "0.00", inclusive = true)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal depositAmount;

    @Size(max = 100)
    private String city;

    @Size(max = 500)
    private String pickupLocation;

    @Valid
    private List<PhotoRequest> photos;
}
