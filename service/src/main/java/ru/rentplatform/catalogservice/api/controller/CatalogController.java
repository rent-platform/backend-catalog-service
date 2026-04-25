package ru.rentplatform.catalogservice.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.catalogservice.api.dto.request.CreateItemRequest;
import ru.rentplatform.catalogservice.api.dto.request.ItemFilterRequest;
import ru.rentplatform.catalogservice.api.dto.request.UpdateItemRequest;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;
import ru.rentplatform.catalogservice.core.service.CatalogService;
import org.springdoc.core.annotations.ParameterObject;

import java.math.BigDecimal;
import java.util.UUID;

import static ru.rentplatform.catalogservice.api.ApiPaths.CATALOG;

@RestController
@RequestMapping(CATALOG)
@RequiredArgsConstructor
@ParameterObject
@Validated
public class CatalogController {

    private final CatalogService catalogService;


    @GetMapping("/items")
    public Page<ItemShortResponse> getActiveItems(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BigDecimal minPricePerDay,
            @RequestParam(required = false) BigDecimal maxPricePerDay,
            @RequestParam(required = false) BigDecimal minPricePerHour,
            @RequestParam(required = false) BigDecimal maxPricePerHour,
            Pageable pageable
    ) {
        UUID currentUserId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;

        ItemFilterRequest filter = ItemFilterRequest.builder()
                .categoryId(categoryId)
                .city(city)
                .query(query)
                .minPricePerDay(minPricePerDay)
                .maxPricePerDay(maxPricePerDay)
                .minPricePerHour(minPricePerHour)
                .maxPricePerHour(maxPricePerHour)
                .build();

        return catalogService.getActiveItems(filter, currentUserId, pageable);
    }

    @GetMapping("/items/{itemId}")
    public ItemResponse getItemById(@AuthenticationPrincipal Jwt jwt,
                                    @PathVariable UUID itemId) {
        UUID currentUserId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        return catalogService.getItemById(itemId, currentUserId);
    }

    @PostMapping("/items")
    @SecurityRequirement(name = "bearerAuth")
    public ItemResponse createItem(@AuthenticationPrincipal Jwt jwt,
                                   @Valid @RequestBody CreateItemRequest request) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return catalogService.createItem(ownerId, request);
    }

    @GetMapping("/my/items")
    @SecurityRequirement(name = "bearerAuth")
    public Page<ItemShortResponse> getMyItems(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ItemStatus status,
            Pageable pageable
    ) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return catalogService.getMyItems(ownerId, status, pageable);
    }

    @PutMapping("/items/{itemId}")
    @SecurityRequirement(name = "bearerAuth")
    public ItemResponse updateMyItem(@AuthenticationPrincipal Jwt jwt,
                                     @PathVariable UUID itemId,
                                     @Valid @RequestBody UpdateItemRequest request) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return catalogService.updateMyItem(ownerId, itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    @SecurityRequirement(name = "bearerAuth")
    public MessageResponse deleteMyItem(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return catalogService.deleteMyItem(ownerId, itemId);
    }

    @GetMapping("/items/{itemId}/similar")
    public Page<ItemShortResponse> getSimilarItems(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID itemId,
            Pageable pageable
    ) {
        UUID currentUserId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        return catalogService.getSimilarItems(itemId, currentUserId, pageable);
    }
}
