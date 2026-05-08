package ru.rentplatform.catalogservice.core.service.implement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.api.exception.ItemNotFoundException;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;
import ru.rentplatform.catalogservice.core.dao.repository.FavoriteItemRepository;
import ru.rentplatform.catalogservice.core.dao.repository.ItemRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteItemRepository favoriteItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    @Test
    void addToFavorites_shouldAdd_whenItemActive() {

        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Item item = Item.builder().id(itemId).status(ItemStatus.ACTIVE).viewsCount(0).build();

        when(itemRepository.findByIdAndDeletedAtIsNull(itemId)).thenReturn(Optional.of(item));
        when(favoriteItemRepository.existsByIdUserIdAndIdItemId(userId, itemId)).thenReturn(false);

        MessageResponse result = favoriteService.addToFavorites(userId, itemId);

        assertNotNull(result);
        verify(favoriteItemRepository).save(any());
    }

    @Test
    void addToFavorites_shouldThrow_whenItemNotFound() {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        when(itemRepository.findByIdAndDeletedAtIsNull(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () ->
                favoriteService.addToFavorites(userId, itemId));
    }

    @Test
    void isFavorite_shouldReturnTrue_whenExists() {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        when(favoriteItemRepository.existsByIdUserIdAndIdItemId(userId, itemId)).thenReturn(true);

        boolean result = favoriteService.isFavorite(userId, itemId);

        assertTrue(result);
    }
}