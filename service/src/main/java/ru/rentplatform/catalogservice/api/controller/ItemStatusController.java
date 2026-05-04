package ru.rentplatform.catalogservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.catalogservice.api.dto.request.RejectItemRequest;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.core.service.ItemStatusService;

import java.util.UUID;

import static ru.rentplatform.catalogservice.api.ApiPaths.CATALOG;
import static ru.rentplatform.catalogservice.core.util.PageableUtils.buildPageable;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Статусы объявлений", description = "Управление статусами объявлений")
public class ItemStatusController {

    private final ItemStatusService itemStatusService;

    @PostMapping(CATALOG + "/items/{itemId}/send-to-moderation")
    @Operation(summary = "Отправить на модерацию", description = "Арендодатель отправляет объявление на модерацию")
    public ItemResponse sendToModeration(@AuthenticationPrincipal Jwt jwt,
                                         @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return itemStatusService.sendToModeration(ownerId, itemId);
    }

    @PostMapping(CATALOG + "/items/{itemId}/return-to-draft")
    @Operation(summary = "Вернуть в черновик", description = "Арендодатель возвращает отклонённое объявление в черновик")
    public ItemResponse returnRejectedToDraft(@AuthenticationPrincipal Jwt jwt,
                                              @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return itemStatusService.returnRejectedToDraft(ownerId, itemId);
    }

    @PostMapping(CATALOG + "/items/{itemId}/archive")
    @Operation(summary = "Архивировать объявление", description = "Арендодатель архивирует своё объявление")
    public ItemResponse archiveItem(@AuthenticationPrincipal Jwt jwt,
                                    @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return itemStatusService.archiveItem(ownerId, itemId);
    }

    @PostMapping(CATALOG + "/admin/items/{itemId}/approve")
    @PreAuthorize("hasAnyRole('moderator', 'admin')")
    @Operation(summary = "Одобрить объявление", description = "Модератор или администратор одобряет объявление")
    public ItemResponse approveItem(@PathVariable UUID itemId) {
        return itemStatusService.approveItem(itemId);
    }

    @PostMapping(CATALOG + "/admin/items/{itemId}/reject")
    @PreAuthorize("hasAnyRole('moderator', 'admin')")
    @Operation(summary = "Отклонить объявление", description = "Модератор или администратор отклоняет " +
            "объявление с причиной")
    public ItemResponse rejectItem(@PathVariable UUID itemId,
                                   @Valid @RequestBody RejectItemRequest request) {
        return itemStatusService.rejectItem(itemId, request);
    }

    @GetMapping(CATALOG + "/admin/items/moderation")
    @PreAuthorize("hasAnyRole('moderator', 'admin')")
    @Operation(summary = "Список на модерации", description = "Получить список объявлений ожидающих модерации")
    public Page<ItemShortResponse> getItemsForModeration(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);

        return itemStatusService.getItemsForModeration(pageable);
    }

    @PostMapping(CATALOG + "/items/{itemId}/restore")
    @Operation(summary = "Восстановить из архива", description = "Владелец восстанавливает архивированное объявление")
    public ItemResponse restoreArchivedItem(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return itemStatusService.restoreArchivedItem(ownerId, itemId);
    }
}
