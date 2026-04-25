package ru.rentplatform.catalogservice.core.service;

import ru.rentplatform.catalogservice.api.dto.request.AddPhotoRequest;
import ru.rentplatform.catalogservice.api.dto.request.ReorderPhotosRequest;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.api.dto.response.PhotoResponse;

import java.util.List;
import java.util.UUID;

public interface PhotoService {

    List<PhotoResponse> getItemPhotos(UUID ownerId, UUID itemId);

    PhotoResponse addPhoto(UUID ownerId, UUID itemId, AddPhotoRequest request);

    MessageResponse deletePhoto(UUID ownerId, UUID itemId, UUID photoId);

    List<PhotoResponse> reorderPhotos(UUID ownerId, UUID itemId, ReorderPhotosRequest request);
}