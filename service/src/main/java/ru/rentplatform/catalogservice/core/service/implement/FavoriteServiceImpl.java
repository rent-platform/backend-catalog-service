package ru.rentplatform.catalogservice.core.service.implement;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.api.exception.ItemNotFoundException;
import ru.rentplatform.catalogservice.core.dao.entity.FavoriteItem;
import ru.rentplatform.catalogservice.core.dao.entity.FavoriteItemId;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;
import ru.rentplatform.catalogservice.core.dao.entity.Photo;
import ru.rentplatform.catalogservice.core.dao.repository.FavoriteItemRepository;
import ru.rentplatform.catalogservice.core.dao.repository.ItemRepository;
import ru.rentplatform.catalogservice.core.dao.repository.PhotoRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;
import ru.rentplatform.catalogservice.core.service.FavoriteService;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteItemRepository favoriteItemRepository;
    private final ItemRepository itemRepository;
    private final PhotoRepository photoRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional
    public MessageResponse addToFavorites(UUID userId, UUID itemId) {
        Item item = itemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.ACTIVE) {
            throw new ItemNotFoundException("Item not found");
        }

        boolean alreadyExists = favoriteItemRepository.existsByIdUserIdAndIdItemId(userId, itemId);
        if (alreadyExists) {
            throw new IllegalArgumentException("Item is already in favorites");
        }

        FavoriteItem favoriteItem = FavoriteItem.builder()
                .id(new FavoriteItemId(userId, itemId))
                .item(item)
                .createdAt(OffsetDateTime.now())
                .build();

        favoriteItemRepository.save(favoriteItem);

        return MessageResponse.builder()
                .message("Item added to favorites")
                .build();
    }

    @Override
    @Transactional
    public MessageResponse removeFromFavorites(UUID userId, UUID itemId) {
        favoriteItemRepository.deleteByIdUserIdAndIdItemId(userId, itemId);

        return MessageResponse.builder()
                .message("Item removed from favorites")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemShortResponse> getMyFavorites(UUID userId, Pageable pageable) {
        return favoriteItemRepository.findAllByIdUserId(userId, pageable)
                .map(favoriteItem -> buildItemShortResponse(favoriteItem.getItem()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(UUID userId, UUID itemId) {
        return favoriteItemRepository.existsByIdUserIdAndIdItemId(userId, itemId);
    }

    private ItemShortResponse buildItemShortResponse(Item item) {
        List<Photo> photos = photoRepository.findAllByItem_IdOrderBySortOrderAsc(item.getId());

        String mainPhotoUrl = photos.stream()
                .min(Comparator.comparing(Photo::getSortOrder))
                .map(Photo::getPhotoUrl)
                .orElse(null);

        ItemShortResponse response = catalogMapper.toItemShortResponse(item);
        response.setMainPhotoUrl(mainPhotoUrl);
        response.setIsFavorite(true);
        return response;
    }
}
