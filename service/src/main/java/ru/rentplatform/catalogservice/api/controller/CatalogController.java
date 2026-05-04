package ru.rentplatform.catalogservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.catalogservice.api.dto.request.CreateItemRequest;
import ru.rentplatform.catalogservice.api.dto.request.ItemFilterRequest;
import ru.rentplatform.catalogservice.api.dto.request.UpdateItemRequest;
import ru.rentplatform.catalogservice.api.dto.response.ItemDealInfoResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;
import ru.rentplatform.catalogservice.core.service.CatalogService;

import java.math.BigDecimal;
import java.util.UUID;

import static ru.rentplatform.catalogservice.api.ApiPaths.CATALOG;
import static ru.rentplatform.catalogservice.core.util.PageableUtils.buildPageable;

@RestController
@RequestMapping(CATALOG)
@RequiredArgsConstructor
@ParameterObject
@Validated
@Tag(name = "Каталог товаров", description = "Поиск, просмотр, создание и управление объявлениями")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/items")
    @Operation(summary = "Список активных объявлений",
            description = "Публичный список объявлений со статусом ACTIVE " +
                    "с фильтрацией по категории, городу, цене и поисковому запросу")
    public Page<ItemShortResponse> getActiveItems(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BigDecimal minPricePerDay,
            @RequestParam(required = false) BigDecimal maxPricePerDay,
            @RequestParam(required = false) BigDecimal minPricePerHour,
            @RequestParam(required = false) BigDecimal maxPricePerHour,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        UUID currentUserId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;

        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);

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
    @Operation(summary = "Детальная информация об объявлении",
            description = "Публичный просмотр объявления. Для владельца показывает объявление в любом статусе")
    public ItemResponse getItemById(@AuthenticationPrincipal Jwt jwt,
                                    @PathVariable UUID itemId) {
        UUID currentUserId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        return catalogService.getItemById(itemId, currentUserId);
    }

    @PostMapping("/items")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Создать объявление",
            description = "Создание нового объявления")
    public ItemResponse createItem(@AuthenticationPrincipal Jwt jwt,
                                   @Valid @RequestBody CreateItemRequest request) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return catalogService.createItem(ownerId, request);
    }

    @GetMapping("/my/items")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Мои объявления",
            description = "Список объявлений текущего пользователя с фильтрацией по статусу")
    public Page<ItemShortResponse> getMyItems(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);

        return catalogService.getMyItems(ownerId, status, pageable);
    }

    @PutMapping("/items/{itemId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Обновить объявление",
            description = "Владелец обновляет своё объявление")
    public ItemResponse updateMyItem(@AuthenticationPrincipal Jwt jwt,
                                     @PathVariable UUID itemId,
                                     @Valid @RequestBody UpdateItemRequest request) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return catalogService.updateMyItem(ownerId, itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Удалить объявление",
            description = "Владелец удаляет своё объявление")
    public MessageResponse deleteMyItem(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return catalogService.deleteMyItem(ownerId, itemId);
    }

    @GetMapping("/items/{itemId}/similar")
    @Operation(summary = "Похожие объявления",
            description = "Публичный список похожих объявлений по категории и городу")
    public Page<ItemShortResponse> getSimilarItems(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID itemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        UUID currentUserId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);

        return catalogService.getSimilarItems(itemId, currentUserId, pageable);
    }

    @GetMapping("/items/{itemId}/deal-info")
    @Operation(summary = "Информация для сделки",
            description = "Публичный эндпоинт для получения данных о товаре при создании сделки")
    public ItemDealInfoResponse getItemDealInfo(@PathVariable UUID itemId) {
        return catalogService.getItemDealInfo(itemId);
    }
}
