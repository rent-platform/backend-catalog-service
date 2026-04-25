package ru.rentplatform.catalogservice.core.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.catalogservice.api.dto.request.AddPhotoRequest;
import ru.rentplatform.catalogservice.api.dto.request.PhotoOrderRequest;
import ru.rentplatform.catalogservice.api.dto.request.ReorderPhotosRequest;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.api.dto.response.PhotoResponse;
import ru.rentplatform.catalogservice.api.exception.AccessDeniedException;
import ru.rentplatform.catalogservice.api.exception.ItemNotFoundException;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.Photo;
import ru.rentplatform.catalogservice.core.dao.repository.ItemRepository;
import ru.rentplatform.catalogservice.core.dao.repository.PhotoRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;
import ru.rentplatform.catalogservice.core.service.PhotoService;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final ItemRepository itemRepository;
    private final PhotoRepository photoRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PhotoResponse> getItemPhotos(UUID ownerId, UUID itemId) {
        Item item = getOwnedItem(ownerId, itemId);

        return photoRepository.findAllByItem_IdOrderBySortOrderAsc(item.getId())
                .stream()
                .map(catalogMapper::toPhotoResponse)
                .toList();
    }

    @Override
    @Transactional
    public PhotoResponse addPhoto(UUID ownerId, UUID itemId, AddPhotoRequest request) {
        Item item = getOwnedItem(ownerId, itemId);

        boolean duplicateUrl = photoRepository.existsByItem_IdAndPhotoUrl(itemId, request.getPhotoUrl());
        if (duplicateUrl) {
            throw new IllegalArgumentException("Photo with this URL already exists for this item");
        }

        int sortOrder = photoRepository.findAllByItem_IdOrderBySortOrderAsc(itemId).size();

        Photo photo = Photo.builder()
                .id(UUID.randomUUID())
                .item(item)
                .photoUrl(request.getPhotoUrl())
                .sortOrder(sortOrder)
                .createdAt(OffsetDateTime.now())
                .build();

        Photo savedPhoto = photoRepository.save(photo);
        return catalogMapper.toPhotoResponse(savedPhoto);
    }

    @Override
    @Transactional
    public MessageResponse deletePhoto(UUID ownerId, UUID itemId, UUID photoId) {
        getOwnedItem(ownerId, itemId);

        Photo photo = photoRepository.findByIdAndItem_Id(photoId, itemId)
                .orElseThrow(() -> new ItemNotFoundException("Photo not found"));

        photoRepository.delete(photo);

        return MessageResponse.builder()
                .message("Photo deleted successfully")
                .build();
    }

    @Override
    @Transactional
    public List<PhotoResponse> reorderPhotos(UUID ownerId, UUID itemId, ReorderPhotosRequest request) {
        getOwnedItem(ownerId, itemId);

        long uniquePhotoIds = request.getPhotos().stream()
                .map(PhotoOrderRequest::getPhotoId)
                .distinct()
                .count();

        if (uniquePhotoIds != request.getPhotos().size()) {
            throw new IllegalArgumentException("Duplicate photoId in reorder request");
        }

        long uniqueSortOrders = request.getPhotos().stream()
                .map(PhotoOrderRequest::getSortOrder)
                .distinct()
                .count();

        if (uniqueSortOrders != request.getPhotos().size()) {
            throw new IllegalArgumentException("Duplicate sortOrder in reorder request");
        }

        List<Photo> existingPhotos = photoRepository.findAllByItem_IdOrderBySortOrderAsc(itemId);

        if (request.getPhotos().size() != existingPhotos.size()) {
            throw new IllegalArgumentException("All item photos must be included in reorder request");
        }

        Map<UUID, Photo> photoMap = existingPhotos.stream()
                .collect(Collectors.toMap(Photo::getId, Function.identity()));

        for (PhotoOrderRequest photoOrder : request.getPhotos()) {
            Photo photo = photoMap.get(photoOrder.getPhotoId());
            if (photo == null) {
                throw new ItemNotFoundException("Photo not found: " + photoOrder.getPhotoId());
            }

            photo.setSortOrder(photoOrder.getSortOrder());
        }

        List<Photo> savedPhotos = photoRepository.saveAll(existingPhotos);

        return savedPhotos.stream()
                .sorted(Comparator.comparing(Photo::getSortOrder))
                .map(catalogMapper::toPhotoResponse)
                .toList();
    }

    private Item getOwnedItem(UUID ownerId, UUID itemId) {
        return itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));
    }
}
