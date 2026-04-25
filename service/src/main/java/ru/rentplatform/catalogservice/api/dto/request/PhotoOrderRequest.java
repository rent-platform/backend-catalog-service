package ru.rentplatform.catalogservice.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoOrderRequest {

    @NotNull
    private UUID photoId;

    @NotNull
    @Min(0)
    private Integer sortOrder;
}