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
import ru.rentplatform.catalogservice.api.exception.CategoryNotFoundException;
import ru.rentplatform.catalogservice.api.exception.ItemNotFoundException;
import ru.rentplatform.catalogservice.core.dao.entity.Category;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;
import ru.rentplatform.catalogservice.core.dao.entity.Photo;
import ru.rentplatform.catalogservice.core.dao.repository.CategoryRepository;
import ru.rentplatform.catalogservice.core.dao.repository.FavoriteItemRepository;
import ru.rentplatform.catalogservice.core.dao.repository.ItemRepository;
import ru.rentplatform.catalogservice.core.dao.repository.PhotoRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;
import ru.rentplatform.catalogservice.core.service.CatalogService;
import ru.rentplatform.catalogservice.integration.client.UserClient;

import java.math.BigDecimal;
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
    private final CatalogMapper catalogMapper;
    private final UserClient userClient;
    private final FavoriteItemRepository favoriteItemRepository;

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

        return buildItemResponse(savedItem, ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(UUID itemId, UUID currentUserId) {
        Item item = itemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.ACTIVE) {
            throw new ItemNotFoundException("Item not found");
        }

        return buildItemResponse(item, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemShortResponse> getActiveItems(ItemFilterRequest filter,
                                                  UUID currentUserId,
                                                  Pageable pageable) {
        Long categoryId = filter != null ? filter.getCategoryId() : null;
        String city = filter != null ? normalizeTextFilter(filter.getCity()) : "";
        String searchQuery = filter != null ? normalizeTextFilter(filter.getQuery()) : "";

        BigDecimal minPricePerDay = filter != null ? filter.getMinPricePerDay() : null;
        BigDecimal maxPricePerDay = filter != null ? filter.getMaxPricePerDay() : null;
        BigDecimal minPricePerHour = filter != null ? filter.getMinPricePerHour() : null;
        BigDecimal maxPricePerHour = filter != null ? filter.getMaxPricePerHour() : null;

        validatePriceRange(minPricePerDay, maxPricePerDay, "price per day");
        validatePriceRange(minPricePerHour, maxPricePerHour, "price per hour");

        return itemRepository.searchActiveItems(
                ItemStatus.ACTIVE,
                categoryId,
                city,
                searchQuery,
                minPricePerDay,
                maxPricePerDay,
                minPricePerHour,
                maxPricePerHour,
                pageable
        ).map(item -> buildItemShortResponse(item, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemShortResponse> getMyItems(UUID ownerId, ItemStatus status, Pageable pageable) {
        Page<Item> itemsPage;

        if (status != null) {
            itemsPage = itemRepository.findAllByOwnerIdAndDeletedAtIsNullAndStatus(
                    ownerId,
                    status,
                    pageable
            );
        } else {
            itemsPage = itemRepository.findAllByOwnerIdAndDeletedAtIsNull(
                    ownerId,
                    pageable
            );
        }

        return itemsPage.map(item -> buildItemShortResponse(item, ownerId));
    }

    @Override
    @Transactional
    public ItemResponse updateMyItem(UUID ownerId, UUID itemId, UpdateItemRequest request) {
        Item item = itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

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

        return buildItemResponse(savedItem, ownerId);
    }

    @Override
    @Transactional
    public MessageResponse deleteMyItem(UUID ownerId, UUID itemId) {
        Item item = itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        item.setDeletedAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        itemRepository.save(item);

        return MessageResponse.builder()
                .message("Item deleted successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemShortResponse> getSimilarItems(UUID itemId,
                                                   UUID currentUserId,
                                                   Pageable pageable) {
        Item item = itemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.ACTIVE) {
            throw new ItemNotFoundException("Item not found");
        }

        return itemRepository.findSimilarItems(
                item.getId(),
                item.getCategory().getId(),
                item.getCity(),
                ItemStatus.ACTIVE,
                pageable
        ).map(similarItem -> buildItemShortResponse(similarItem, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDealInfoResponse getItemDealInfo(UUID itemId) {
        Item item = itemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        return ItemDealInfoResponse.builder()
                .id(item.getId())
                .ownerId(item.getOwnerId())
                .status(item.getStatus().name())
                .pricePerDay(item.getPricePerDay())
                .pricePerHour(item.getPricePerHour())
                .depositAmount(item.getDepositAmount())
                .build();
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

    private ItemResponse buildItemResponse(Item item, UUID currentUserId) {
        List<PhotoResponse> photos = photoRepository.findAllByItem_IdOrderBySortOrderAsc(item.getId())
                .stream()
                .map(catalogMapper::toPhotoResponse)
                .toList();

        ItemResponse response = catalogMapper.toItemResponse(item);
        response.setPhotos(photos);
        response.setIsFavorite(isFavorite(currentUserId, item.getId()));

        var ownerData = userClient.getUserPublicProfile(item.getOwnerId());

        response.setOwner(
                OwnerShortResponse.builder()
                        .id(ownerData.getId())
                        .nickname(ownerData.getNickname())
                        .avatarUrl(ownerData.getAvatarUrl())
                        .rating(ownerData.getRating())
                        .build()
        );

        return response;
    }

    private ItemShortResponse buildItemShortResponse(Item item, UUID currentUserId) {
        List<Photo> photos = photoRepository.findAllByItem_IdOrderBySortOrderAsc(item.getId());

        String mainPhotoUrl = photos.stream()
                .min(Comparator.comparing(Photo::getSortOrder))
                .map(Photo::getPhotoUrl)
                .orElse(null);

        ItemShortResponse response = catalogMapper.toItemShortResponse(item);
        response.setMainPhotoUrl(mainPhotoUrl);
        response.setIsFavorite(isFavorite(currentUserId, item.getId()));

        return response;
    }

    private void validatePrices(BigDecimal pricePerDay, BigDecimal pricePerHour) {
        if (pricePerDay == null && pricePerHour == null) {
            throw new IllegalArgumentException("At least one price must be specified");
        }
    }

    private Boolean isFavorite(UUID currentUserId, UUID itemId) {
        if (currentUserId == null) {
            return false;
        }

        return favoriteItemRepository.existsByIdUserIdAndIdItemId(currentUserId, itemId);
    }

    private String normalizeTextFilter(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed.toLowerCase();
    }

    private void validatePriceRange(BigDecimal min, BigDecimal max, String fieldName) {
        if (min != null && min.signum() < 0) {
            throw new IllegalArgumentException("Minimum " + fieldName + " cannot be negative");
        }

        if (max != null && max.signum() < 0) {
            throw new IllegalArgumentException("Maximum " + fieldName + " cannot be negative");
        }

        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Minimum " + fieldName + " cannot be greater than maximum " + fieldName);
        }
    }
}
