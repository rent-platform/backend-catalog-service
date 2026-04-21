package ru.rentplatform.catalogservice.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    @Size(max = 100)
    private String categoryName;

    @Size(max = 120)
    private String slug;

    private Long parentId;

    private Integer sortOrder;

    private Boolean isActive;
}
