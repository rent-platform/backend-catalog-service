package ru.rentplatform.catalogservice.core.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.catalogservice.api.dto.request.RejectItemRequest;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.api.dto.response.PhotoResponse;
import ru.rentplatform.catalogservice.api.exception.InvalidItemStatusException;
import ru.rentplatform.catalogservice.api.exception.ItemNotFoundException;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;
import ru.rentplatform.catalogservice.core.dao.entity.Photo;
import ru.rentplatform.catalogservice.core.dao.repository.ItemRepository;
import ru.rentplatform.catalogservice.core.dao.repository.PhotoRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;
import ru.rentplatform.catalogservice.core.service.ItemStatusService;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemStatusServiceImpl implements ItemStatusService {

    private final ItemRepository itemRepository;
    private final PhotoRepository photoRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional
    public ItemResponse sendToModeration(UUID ownerId, UUID itemId) {
        Item item = itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.DRAFT) {
            throw new InvalidItemStatusException("Only draft item can be sent to moderation");
        }

        validateItemBeforeModeration(item);

        item.setStatus(ItemStatus.MODERATION);
        item.setModerationComment(null);
        item.setUpdatedAt(OffsetDateTime.now());

        Item savedItem = itemRepository.save(item);
        return buildItemResponse(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse approveItem(UUID itemId) {
        Item item = itemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.MODERATION) {
            throw new InvalidItemStatusException("Only item in moderation can be approved");
        }

        item.setStatus(ItemStatus.ACTIVE);
        item.setModerationComment(null);
        item.setUpdatedAt(OffsetDateTime.now());

        Item savedItem = itemRepository.save(item);
        return buildItemResponse(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse rejectItem(UUID itemId, RejectItemRequest request) {
        Item item = itemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.MODERATION) {
            throw new InvalidItemStatusException("Only item in moderation can be rejected");
        }

        item.setStatus(ItemStatus.REJECTED);
        item.setModerationComment(request.getModerationComment());
        item.setUpdatedAt(OffsetDateTime.now());

        Item savedItem = itemRepository.save(item);
        return buildItemResponse(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse archiveItem(UUID ownerId, UUID itemId) {
        Item item = itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.ACTIVE) {
            throw new InvalidItemStatusException("Only active item can be archived");
        }

        item.setStatus(ItemStatus.ARCHIVED);
        item.setUpdatedAt(OffsetDateTime.now());

        Item savedItem = itemRepository.save(item);
        return buildItemResponse(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse returnRejectedToDraft(UUID ownerId, UUID itemId) {
        Item item = itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.REJECTED) {
            throw new InvalidItemStatusException("Only rejected item can be returned to draft");
        }

        item.setStatus(ItemStatus.DRAFT);
        item.setUpdatedAt(OffsetDateTime.now());

        Item savedItem = itemRepository.save(item);
        return buildItemResponse(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemShortResponse> getItemsForModeration(Pageable pageable) {
        return itemRepository.findAllByDeletedAtIsNullAndStatus(ItemStatus.MODERATION, pageable)
                .map(this::buildItemShortResponse);
    }

    @Override
    @Transactional
    public ItemResponse restoreArchivedItem(UUID ownerId, UUID itemId) {
        Item item = itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.ARCHIVED) {
            throw new InvalidItemStatusException("Only archived items can be restored");
        }

        item.setStatus(ItemStatus.DRAFT);
        item.setModerationComment(null);
        item.setUpdatedAt(OffsetDateTime.now());

        Item savedItem = itemRepository.save(item);
        return buildItemResponse(savedItem);
    }

    private void validateItemBeforeModeration(Item item) {
        if (item.getCategory() == null) {
            throw new InvalidItemStatusException("Category is required");
        }

        if (item.getTitle() == null || item.getTitle().isBlank()) {
            throw new InvalidItemStatusException("Title is required");
        }

        if (item.getCity() == null || item.getCity().isBlank()) {
            throw new InvalidItemStatusException("City is required");
        }

        if (item.getPickupLocation() == null || item.getPickupLocation().isBlank()) {
            throw new InvalidItemStatusException("Pickup location is required");
        }

        if (item.getPricePerDay() == null && item.getPricePerHour() == null) {
            throw new InvalidItemStatusException("At least one price must be specified");
        }

        boolean hasPhotos = !photoRepository.findAllByItem_IdOrderBySortOrderAsc(item.getId()).isEmpty();
        if (!hasPhotos) {
            throw new InvalidItemStatusException("At least one photo is required");
        }
    }

    private ItemResponse buildItemResponse(Item item) {
        List<PhotoResponse> photos = photoRepository.findAllByItem_IdOrderBySortOrderAsc(item.getId())
                .stream()
                .map(catalogMapper::toPhotoResponse)
                .toList();

        ItemResponse response = catalogMapper.toItemResponse(item);
        response.setPhotos(photos);
        return response;
    }

    private ItemShortResponse buildItemShortResponse(Item item) {
        List<Photo> photos = photoRepository.findAllByItem_IdOrderBySortOrderAsc(item.getId());

        String mainPhotoUrl = photos.stream()
                .min(Comparator.comparing(Photo::getSortOrder))
                .map(Photo::getPhotoUrl)
                .orElse(null);

        ItemShortResponse response = catalogMapper.toItemShortResponse(item);
        response.setMainPhotoUrl(mainPhotoUrl);
        return response;
    }
}
