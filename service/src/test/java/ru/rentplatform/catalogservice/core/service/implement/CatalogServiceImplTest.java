package ru.rentplatform.catalogservice.core.service.implement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rentplatform.catalogservice.api.dto.request.CreateItemRequest;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.api.exception.CategoryNotFoundException;
import ru.rentplatform.catalogservice.api.exception.ItemNotFoundException;
import ru.rentplatform.catalogservice.core.dao.entity.Category;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;
import ru.rentplatform.catalogservice.core.dao.repository.CategoryRepository;
import ru.rentplatform.catalogservice.core.dao.repository.FavoriteItemRepository;
import ru.rentplatform.catalogservice.core.dao.repository.ItemRepository;
import ru.rentplatform.catalogservice.core.dao.repository.PhotoRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;
import ru.rentplatform.catalogservice.integration.client.UserClient;
import ru.rentplatform.catalogservice.integration.dto.UserPublicResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @Mock
    private UserClient userClient;

    @Mock private FavoriteItemRepository favoriteItemRepository;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private UUID itemId;

    private UUID ownerId;

    private Long categoryId;

    private Item item;

    private Category category;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        categoryId = 1L;

        category = Category.builder()
                .id(categoryId).categoryName("Test").slug("test")
                .isActive(true).sortOrder(0).build();

        item = Item.builder()
                .id(itemId).ownerId(ownerId).category(category)
                .title("Test Item").city("Moscow")
                .pricePerDay(new BigDecimal("500"))
                .pricePerHour(BigDecimal.ZERO)
                .depositAmount(new BigDecimal("1000"))
                .status(ItemStatus.ACTIVE)
                .viewsCount(0).build();

        lenient().when(favoriteItemRepository.existsByIdUserIdAndIdItemId(any(), any())).thenReturn(false);
        lenient().when(userClient.getUserPublicProfile(any())).thenReturn(
                UserPublicResponse.builder()
                        .id(ownerId).nickname("Test").avatarUrl(null).rating(0.0).build());
    }

    @Test
    void getItemById_shouldReturnItem_whenActive() {
        when(itemRepository.findByIdAndDeletedAtIsNull(itemId)).thenReturn(Optional.of(item));
        when(photoRepository.findAllByItem_IdOrderBySortOrderAsc(itemId)).thenReturn(List.of());
        when(catalogMapper.toItemResponse(item)).thenReturn(new ItemResponse());

        ItemResponse result = catalogService.getItemById(itemId, null, null);

        assertNotNull(result);
        assertEquals(1, item.getViewsCount());
    }

    @Test
    void getItemById_shouldThrow_whenNotFound() {
        when(itemRepository.findByIdAndDeletedAtIsNull(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () ->
                catalogService.getItemById(itemId, null, null));
    }

    @Test
    void getItemById_shouldNotIncrementViews_whenOwner() {
        when(itemRepository.findByIdAndDeletedAtIsNull(itemId)).thenReturn(Optional.of(item));
        when(photoRepository.findAllByItem_IdOrderBySortOrderAsc(itemId)).thenReturn(List.of());
        when(catalogMapper.toItemResponse(item)).thenReturn(new ItemResponse());

        catalogService.getItemById(itemId, ownerId, null);

        assertEquals(0, item.getViewsCount());
    }

    @Test
    void createItem_shouldCreate_whenValid() {
        CreateItemRequest request = CreateItemRequest.builder()
                .categoryId(categoryId).title("New Item")
                .pricePerDay(new BigDecimal("300")).pricePerHour(BigDecimal.ZERO)
                .depositAmount(BigDecimal.ZERO).city("SPB").build();

        when(categoryRepository.findByIdAndDeletedAtIsNullAndIsActiveTrue(categoryId))
                .thenReturn(Optional.of(category));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(photoRepository.findAllByItem_IdOrderBySortOrderAsc(itemId)).thenReturn(List.of());
        when(catalogMapper.toItemResponse(any(Item.class))).thenReturn(new ItemResponse());

        ItemResponse result = catalogService.createItem(ownerId, request);

        assertNotNull(result);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_shouldThrow_whenCategoryNotFound() {
        CreateItemRequest request = CreateItemRequest.builder()
                .categoryId(categoryId).title("New Item")
                .pricePerDay(new BigDecimal("300")).pricePerHour(BigDecimal.ZERO)
                .depositAmount(BigDecimal.ZERO).build();

        when(categoryRepository.findByIdAndDeletedAtIsNullAndIsActiveTrue(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () ->
                catalogService.createItem(ownerId, request));
    }
}
