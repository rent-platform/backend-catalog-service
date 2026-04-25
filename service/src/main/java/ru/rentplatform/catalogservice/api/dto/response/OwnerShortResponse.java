package ru.rentplatform.catalogservice.api.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerShortResponse {

    private UUID id;

    private String nickname;

    private String avatarUrl;

    private Double rating;
}
