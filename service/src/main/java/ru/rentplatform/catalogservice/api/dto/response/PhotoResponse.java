package ru.rentplatform.catalogservice.api.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {

    private UUID id;

    private String photoUrl;

    private Integer sortOrder;
}
