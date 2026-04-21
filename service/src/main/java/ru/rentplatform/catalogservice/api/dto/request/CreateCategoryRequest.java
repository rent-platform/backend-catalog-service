package ru.rentplatform.catalogservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank
    @Size(max = 100)
    private String categoryName;

    @NotBlank
    @Size(max = 120)
    private String slug;

    private Long parentId;

    @NotNull
    private Integer sortOrder;

    @NotNull
    private Boolean isActive;
}