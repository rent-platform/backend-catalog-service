package ru.rentplatform.catalogservice.integration.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicResponse {

    private UUID id;

    private String nickname;

    private String avatarUrl;

    private Double rating;
}
