package ru.rentplatform.catalogservice.core.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.catalogservice.api.dto.request.CreateItemRequest;
import ru.rentplatform.catalogservice.api.dto.request.ItemFilterRequest;
import ru.rentplatform.catalogservice.api.dto.request.PhotoRequest;
import ru.rentplatform.catalogservice.api.dto.request.UpdateItemRequest;
import ru.rentplatform.catalogservice.api.dto.response.*;
import ru.rentplatform.catalogservice.api.exception.AccessDeniedException;
import ru.rentplatform.catalogservice.api.exception.CategoryNotFoundException;
import ru.rentplatform.catalogservice.api.exception.ItemNotFoundException;
import ru.rentplatform.catalogservice.core.dao.entity.*;
import ru.rentplatform.catalogservice.core.dao.repository.*;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;
import ru.rentplatform.catalogservice.core.service.CatalogService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final PhotoRepository photoRepository;
    private final AvailabilityRepository availabilityRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional
    public ItemResponse createItem(UUID ownerId, CreateItemRequest request) {
        validatePrices(request.getPricePerDay(), request.getPricePerHour());

        Category category = categoryRepository.findByIdAndDeletedAtIsNullAndIsActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        OffsetDateTime now = OffsetDateTime.now();

        Item item = Item.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .category(category)
                .title(request.getTitle())
                .itemDescription(request.getItemDescription())
                .pricePerDay(request.getPricePerDay())
                .pricePerHour(request.getPricePerHour())
                .depositAmount(request.getDepositAmount())
                .city(request.getCity())
                .pickupLocation(request.getPickupLocation())
                .status(ItemStatus.DRAFT)
                .moderationComment(null)
                .viewsCount(0)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();

        Item savedItem = itemRepository.save(item);

        savePhotos(savedItem, request.getPhotos());

        return buildItemResponse(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(UUID itemId) {
        Item item = itemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.ACTIVE) {
            throw new ItemNotFoundException("Item not found");
        }

        return buildItemResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemShortResponse> getActiveItems(ItemFilterRequest filter, Pageable pageable) {
        Page<Item> itemsPage;

        if (filter != null && filter.getCategoryId() != null) {
            itemsPage = itemRepository.findAllByCategoryIdAndDeletedAtIsNullAndStatus(
                    filter.getCategoryId(),
                    ItemStatus.ACTIVE,
                    pageable
            );
        } else if (filter != null && filter.getCity() != null && !filter.getCity().isBlank()) {
            itemsPage = itemRepository.findAllByCityIgnoreCaseAndDeletedAtIsNullAndStatus(
                    filter.getCity().trim(),
                    ItemStatus.ACTIVE,
                    pageable
            );
        } else if (filter != null && filter.getQuery() != null && !filter.getQuery().isBlank()) {
            itemsPage = itemRepository.findAllByTitleContainingIgnoreCaseAndDeletedAtIsNullAndStatus(
                    filter.getQuery().trim(),
                    ItemStatus.ACTIVE,
                    pageable
            );
        } else {
            itemsPage = itemRepository.findAllByDeletedAtIsNullAndStatus(ItemStatus.ACTIVE, pageable);
        }

        return itemsPage.map(this::buildItemShortResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemShortResponse> getMyItems(UUID ownerId, Pageable pageable) {
        return itemRepository.findAllByOwnerIdAndDeletedAtIsNull(ownerId, pageable)
                .map(this::buildItemShortResponse);
    }

    @Override
    @Transactional
    public ItemResponse updateMyItem(UUID ownerId, UUID itemId, UpdateItemRequest request) {
        Item item = itemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("You do not have access to this item");
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedAtIsNullAndIsActiveTrue(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
            item.setCategory(category);
        }

        if (request.getTitle() != null) {
            item.setTitle(request.getTitle());
        }

        if (request.getItemDescription() != null) {
            item.setItemDescription(request.getItemDescription());
        }

        if (request.getPricePerDay() != null) {
            item.setPricePerDay(request.getPricePerDay());
        }

        if (request.getPricePerHour() != null) {
            item.setPricePerHour(request.getPricePerHour());
        }

        validatePrices(item.getPricePerDay(), item.getPricePerHour());

        if (request.getDepositAmount() != null) {
            item.setDepositAmount(request.getDepositAmount());
        }

        if (request.getCity() != null) {
            item.setCity(request.getCity());
        }

        if (request.getPickupLocation() != null) {
            item.setPickupLocation(request.getPickupLocation());
        }

        item.setUpdatedAt(OffsetDateTime.now());

        Item savedItem = itemRepository.save(item);

        if (request.getPhotos() != null) {
            photoRepository.deleteAllByItem_Id(savedItem.getId());
            savePhotos(savedItem, request.getPhotos());
        }

        return buildItemResponse(savedItem);
    }

    @Override
    @Transactional
    public MessageResponse deleteMyItem(UUID ownerId, UUID itemId) {
        Item item = itemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("You do not have access to this item");
        }

        item.setDeletedAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        itemRepository.save(item);

        return MessageResponse.builder()
                .message("Item deleted successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(catalogMapper::toCategoryResponse)
                .toList();
    }

    private void savePhotos(Item item, List<PhotoRequest> photoRequests) {
        if (photoRequests == null || photoRequests.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        List<Photo> photos = new ArrayList<>();
        for (PhotoRequest photoRequest : photoRequests) {
            photos.add(Photo.builder()
                    .id(UUID.randomUUID())
                    .item(item)
                    .photoUrl(photoRequest.getPhotoUrl())
                    .sortOrder(photoRequest.getSortOrder() != null ? photoRequest.getSortOrder() : 0)
                    .createdAt(now)
                    .build());
        }

        photoRepository.saveAll(photos);
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

    private void validatePrices(java.math.BigDecimal pricePerDay, java.math.BigDecimal pricePerHour) {
        if (pricePerDay == null && pricePerHour == null) {
            throw new IllegalArgumentException("At least one price must be specified");
        }
    }
}
