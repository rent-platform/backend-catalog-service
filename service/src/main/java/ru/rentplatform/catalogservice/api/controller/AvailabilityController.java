package ru.rentplatform.catalogservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.catalogservice.api.dto.request.AvailabilityDeleteRequest;
import ru.rentplatform.catalogservice.api.dto.request.AvailabilityRequest;
import ru.rentplatform.catalogservice.api.dto.response.AvailabilityResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.core.service.AvailabilityService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static ru.rentplatform.catalogservice.api.ApiPaths.CATALOG;

@RestController
@RequestMapping(CATALOG + "/items")
@RequiredArgsConstructor
@Validated
@Tag(name = "Календарь доступности", description = "Управление доступностью товаров по датам")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/{itemId}/availability")
    @Operation(summary = "Получить календарь доступности товара",
            description = "Возвращает список дат с флагом доступности за указанный период. " +
                    "Для гостей — только ACTIVE товары. Для владельца — в любом статусе.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public List<AvailabilityResponse> getAvailability(
            @Parameter(description = "ID товара") @PathVariable UUID itemId,
            @Parameter(description = "Начало периода (yyyy-MM-dd)", example = "2026-05-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Конец периода (yyyy-MM-dd)", example = "2026-05-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID currentUserId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        return availabilityService.getAvailability(itemId, startDate, endDate, currentUserId);
    }

    @PutMapping("/{itemId}/availability")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Установить доступность товара (арендатор)",
            description = "Создаёт или обновляет слоты доступности. Только для владельца товара.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public List<AvailabilityResponse> setAvailability(
            @Parameter(description = "ID товара") @PathVariable UUID itemId,
            @Valid @RequestBody AvailabilityRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return availabilityService.setAvailability(itemId, ownerId, request.getSlots());
    }

    @DeleteMapping("/{itemId}/availability")
    @Operation(summary = "Удалить слоты доступности (арендатор)",
            description = "Удаляет все записи доступности за указанный период. Только для владельца товара.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public MessageResponse deleteAvailability(
            @Parameter(description = "ID товара") @PathVariable UUID itemId,
            @Valid @RequestBody AvailabilityDeleteRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return availabilityService.deleteAvailability(itemId, ownerId, request.getStartDate(), request.getEndDate());
    }
}
