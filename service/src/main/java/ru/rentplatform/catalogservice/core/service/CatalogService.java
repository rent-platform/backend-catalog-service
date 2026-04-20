package ru.rentplatform.catalogservice.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.rentplatform.catalogservice.api.dto.request.CreateItemRequest;
import ru.rentplatform.catalogservice.api.dto.request.ItemFilterRequest;
import ru.rentplatform.catalogservice.api.dto.request.UpdateItemRequest;
import ru.rentplatform.catalogservice.api.dto.response.CategoryResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface CatalogService {

    ItemResponse createItem(UUID ownerId, CreateItemRequest request);

    ItemResponse getItemById(UUID itemId);

    Page<ItemShortResponse> getActiveItems(ItemFilterRequest filter, Pageable pageable);

    Page<ItemShortResponse> getMyItems(UUID ownerId, Pageable pageable);

    ItemResponse updateMyItem(UUID ownerId, UUID itemId, UpdateItemRequest request);

    MessageResponse deleteMyItem(UUID ownerId, UUID itemId);

    List<CategoryResponse> getActiveCategories();
}
