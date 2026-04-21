package ru.rentplatform.catalogservice.core.service;

import org.springframework.data.domain.Page;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;

import java.awt.print.Pageable;
import java.util.UUID;

public interface FavoriteService {

    MessageResponse addToFavorites(UUID userId, UUID itemId);

    MessageResponse removeFromFavorites(UUID userId, UUID itemId);

    Page<ItemShortResponse> getMyFavorites(UUID userId, Pageable pageable);

    boolean isFavorite(UUID userId, UUID itemId);
}

