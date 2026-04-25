package ru.rentplatform.catalogservice.integration.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.rentplatform.catalogservice.integration.dto.UserPublicResponse;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestClient userServiceRestClient;

    public UserPublicResponse getUserPublicProfile(UUID userId) {
        return userServiceRestClient.get()
                .uri("/api/users/{userId}/public", userId)
                .retrieve()
                .body(UserPublicResponse.class);
    }
}