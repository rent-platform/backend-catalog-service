package ru.rentplatform.catalogservice.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.core.service.FavoriteService;

import java.util.UUID;

import static ru.rentplatform.catalogservice.api.ApiPaths.CATALOG;
import static ru.rentplatform.catalogservice.core.util.PageableUtils.buildPageable;

@RestController
@RequestMapping(CATALOG + "/favorites")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{itemId}")
    public MessageResponse addToFavorites(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable UUID itemId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return favoriteService.addToFavorites(userId, itemId);
    }

    @DeleteMapping("/{itemId}")
    public MessageResponse removeFromFavorites(@AuthenticationPrincipal Jwt jwt,
                                               @PathVariable UUID itemId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return favoriteService.removeFromFavorites(userId, itemId);
    }

    @GetMapping("/my")
    public Page<ItemShortResponse> getMyFavorites(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);

        return favoriteService.getMyFavorites(userId, pageable);
    }

    @GetMapping("/{itemId}/status")
    public boolean isFavorite(@AuthenticationPrincipal Jwt jwt,
                              @PathVariable UUID itemId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return favoriteService.isFavorite(userId, itemId);
    }
}