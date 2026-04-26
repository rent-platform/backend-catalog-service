package ru.rentplatform.catalogservice.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class ItemStatusController {

    private final ItemStatusService itemStatusService;

    @PostMapping(CATALOG + "/items/{itemId}/send-to-moderation")
    public ItemResponse sendToModeration(@AuthenticationPrincipal Jwt jwt,
                                         @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return itemStatusService.sendToModeration(ownerId, itemId);
    }

    @PostMapping(CATALOG + "/items/{itemId}/return-to-draft")
    public ItemResponse returnRejectedToDraft(@AuthenticationPrincipal Jwt jwt,
                                              @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return itemStatusService.returnRejectedToDraft(ownerId, itemId);
    }

    @PostMapping(CATALOG + "/items/{itemId}/archive")
    public ItemResponse archiveItem(@AuthenticationPrincipal Jwt jwt,
                                    @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return itemStatusService.archiveItem(ownerId, itemId);
    }

    @PostMapping(CATALOG + "/admin/items/{itemId}/approve")
    public ItemResponse approveItem(@PathVariable UUID itemId) {
        return itemStatusService.approveItem(itemId);
    }

    @PostMapping(CATALOG + "/admin/items/{itemId}/reject")
    public ItemResponse rejectItem(@PathVariable UUID itemId,
                                   @Valid @RequestBody RejectItemRequest request) {
        return itemStatusService.rejectItem(itemId, request);
    }

    @GetMapping(CATALOG + "/admin/items/moderation")
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
    public ItemResponse restoreArchivedItem(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return itemStatusService.restoreArchivedItem(ownerId, itemId);
    }
}
