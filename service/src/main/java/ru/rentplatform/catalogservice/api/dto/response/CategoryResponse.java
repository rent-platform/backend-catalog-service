package ru.rentplatform.catalogservice.api.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;

    private String categoryName;

    private String slug;

    private Long parentId;

    private Integer sortOrder;

    private Boolean isActive;
}