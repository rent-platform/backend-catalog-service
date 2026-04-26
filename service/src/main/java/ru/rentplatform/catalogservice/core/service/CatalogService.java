package ru.rentplatform.catalogservice.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.rentplatform.catalogservice.api.dto.request.CreateItemRequest;
import ru.rentplatform.catalogservice.api.dto.request.ItemFilterRequest;
import ru.rentplatform.catalogservice.api.dto.request.UpdateItemRequest;
import ru.rentplatform.catalogservice.api.dto.response.*;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;

import java.util.List;
import java.util.UUID;

public interface CatalogService {

    ItemResponse createItem(UUID ownerId, CreateItemRequest request);

    ItemResponse getItemById(UUID itemId, UUID currentUserId);

    Page<ItemShortResponse> getActiveItems(ItemFilterRequest filter, UUID currentUserId, Pageable pageable);

    Page<ItemShortResponse> getMyItems(UUID ownerId, ItemStatus status, Pageable pageable);

    ItemResponse updateMyItem(UUID ownerId, UUID itemId, UpdateItemRequest request);

    MessageResponse deleteMyItem(UUID ownerId, UUID itemId);

    Page<ItemShortResponse> getSimilarItems(UUID itemId, UUID currentUserId, Pageable pageable);

    ItemDealInfoResponse getItemDealInfo(UUID itemId);
}
