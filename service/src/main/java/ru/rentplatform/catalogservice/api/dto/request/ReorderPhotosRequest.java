package ru.rentplatform.catalogservice.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderPhotosRequest {

    @Valid
    @NotEmpty
    private List<PhotoOrderRequest> photos;
}