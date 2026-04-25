package ru.rentplatform.catalogservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPhotoRequest {

    @NotBlank
    @Size(max = 2000)
    private String photoUrl;

    private Integer sortOrder;
}