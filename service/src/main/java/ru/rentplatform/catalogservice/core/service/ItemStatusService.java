package ru.rentplatform.catalogservice.core.service;

import ru.rentplatform.catalogservice.api.dto.request.RejectItemRequest;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;

import java.util.UUID;

public interface ItemStatusService {

    ItemResponse sendToModeration(UUID ownerId, UUID itemId);

    ItemResponse approveItem(UUID itemId);

    ItemResponse rejectItem(UUID itemId, RejectItemRequest request);

    ItemResponse archiveItem(UUID ownerId, UUID itemId);

    ItemResponse returnRejectedToDraft(UUID ownerId, UUID itemId);
}
