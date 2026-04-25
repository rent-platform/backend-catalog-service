package ru.rentplatform.catalogservice.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.rentplatform.catalogservice.api.dto.request.RejectItemRequest;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;


import java.util.UUID;

public interface ItemStatusService {

    ItemResponse sendToModeration(UUID ownerId, UUID itemId);

    ItemResponse approveItem(UUID itemId);

    ItemResponse rejectItem(UUID itemId, RejectItemRequest request);

    ItemResponse archiveItem(UUID ownerId, UUID itemId);

    ItemResponse returnRejectedToDraft(UUID ownerId, UUID itemId);

    Page<ItemShortResponse> getItemsForModeration(Pageable pageable);

    ItemResponse restoreArchivedItem(UUID ownerId, UUID itemId);
}
