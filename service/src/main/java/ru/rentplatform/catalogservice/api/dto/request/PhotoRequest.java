package ru.rentplatform.catalogservice.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoRequest {

    @NotBlank
    @Size(max = 2000)
    private String photoUrl;

    @Min(0)
    private Integer sortOrder;
}
