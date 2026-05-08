package ru.rentplatform.catalogservice.core.service.implement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rentplatform.catalogservice.api.dto.response.PhotoResponse;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.Photo;
import ru.rentplatform.catalogservice.core.dao.repository.ItemRepository;
import ru.rentplatform.catalogservice.core.dao.repository.PhotoRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @InjectMocks
    private PhotoServiceImpl photoService;

    @Test
    void getItemPhotos_shouldReturnList_whenItemExists() {

        UUID itemId = UUID.randomUUID();
        Item item = Item.builder().id(itemId).build();
        Photo photo = Photo.builder().id(UUID.randomUUID()).photoUrl("https://example.com/1.jpg").build();
        PhotoResponse response = PhotoResponse.builder().photoUrl("https://example.com/1.jpg").build();

        when(itemRepository.findByIdAndDeletedAtIsNull(itemId)).thenReturn(Optional.of(item));
        when(photoRepository.findAllByItem_IdOrderBySortOrderAsc(itemId)).thenReturn(List.of(photo));
        when(catalogMapper.toPhotoResponse(photo)).thenReturn(response);

        List<PhotoResponse> result = photoService.getItemPhotos(itemId);

        assertEquals(1, result.size());
        assertEquals("https://example.com/1.jpg", result.get(0).getPhotoUrl());
    }
}
