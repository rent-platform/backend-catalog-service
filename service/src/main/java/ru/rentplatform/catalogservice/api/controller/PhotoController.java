package ru.rentplatform.catalogservice.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.rentplatform.catalogservice.api.dto.request.AddPhotoRequest;
import ru.rentplatform.catalogservice.api.dto.request.ReorderPhotosRequest;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.api.dto.response.PhotoResponse;
import ru.rentplatform.catalogservice.core.service.PhotoService;

import java.util.List;
import java.util.UUID;

import static ru.rentplatform.catalogservice.api.ApiPaths.CATALOG;

@RestController
@RequestMapping(CATALOG + "/items/{itemId}/photos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PhotoController {

    private final PhotoService photoService;

    @GetMapping
    public List<PhotoResponse> getItemPhotos(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable UUID itemId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return photoService.getItemPhotos(ownerId, itemId);
    }

    @PostMapping
    public PhotoResponse addPhoto(@AuthenticationPrincipal Jwt jwt,
                                  @PathVariable UUID itemId,
                                  @Valid @RequestBody AddPhotoRequest request) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return photoService.addPhoto(ownerId, itemId, request);
    }

    @DeleteMapping("/{photoId}")
    public MessageResponse deletePhoto(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID itemId,
                                       @PathVariable UUID photoId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return photoService.deletePhoto(ownerId, itemId, photoId);
    }

    @PutMapping("/order")
    public List<PhotoResponse> reorderPhotos(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable UUID itemId,
                                             @Valid @RequestBody ReorderPhotosRequest request) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        return photoService.reorderPhotos(ownerId, itemId, request);
    }
}
