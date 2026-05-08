package ru.rentplatform.catalogservice.core.service.implement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rentplatform.catalogservice.api.dto.request.AvailabilityRequest;
import ru.rentplatform.catalogservice.api.dto.response.AvailabilityResponse;
import ru.rentplatform.catalogservice.core.dao.entity.Availability;
import ru.rentplatform.catalogservice.core.dao.entity.AvailabilityId;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;
import ru.rentplatform.catalogservice.core.dao.repository.AvailabilityRepository;
import ru.rentplatform.catalogservice.core.dao.repository.ItemRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    private UUID itemId;

    private UUID ownerId;

    @BeforeEach
    void setUp() {

        itemId = UUID.randomUUID();

        ownerId = UUID.randomUUID();
    }

    @Test
    void getAvailability_shouldReturnSlots() {
        Item item = Item.builder().id(itemId).ownerId(ownerId)
                .status(ItemStatus.ACTIVE).viewsCount(0).build();

        Availability availability = Availability.builder()
                .id(AvailabilityId.builder().itemId(itemId).availableDate(LocalDate.now()).build())
                .isAvailable(true).build();

        when(itemRepository.findByIdAndDeletedAtIsNull(itemId)).thenReturn(Optional.of(item));
        when(availabilityRepository.findByItemIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(availability));
        when(catalogMapper.toAvailabilityResponseList(any())).thenReturn(List.of());

        List<AvailabilityResponse> result = availabilityService.getAvailability(
                itemId, LocalDate.now(), LocalDate.now().plusDays(7), null);

        assertNotNull(result);
    }

    @Test
    void setAvailability_shouldSaveSlots() {
        Item item = Item.builder().id(itemId).ownerId(ownerId)
                .status(ItemStatus.DRAFT).viewsCount(0).build();

        AvailabilityRequest.AvailabilitySlot slot = new AvailabilityRequest.AvailabilitySlot();
        slot.setDate(LocalDate.now().plusDays(1));
        slot.setIsAvailable(true);

        when(itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId))
                .thenReturn(Optional.of(item));
        when(availabilityRepository.findById(any())).thenReturn(Optional.empty());
        when(availabilityRepository.saveAll(any())).thenReturn(List.of());
        when(catalogMapper.toAvailabilityResponseList(any())).thenReturn(List.of());

        List<AvailabilityResponse> result = availabilityService.setAvailability(
                itemId, ownerId, List.of(slot));

        assertNotNull(result);
        verify(availabilityRepository).saveAll(any());
    }

    @Test
    void setAvailability_shouldThrow_whenPastDate() {
        Item item = Item.builder().id(itemId).ownerId(ownerId)
                .status(ItemStatus.DRAFT).viewsCount(0).build();

        AvailabilityRequest.AvailabilitySlot slot = new AvailabilityRequest.AvailabilitySlot();
        slot.setDate(LocalDate.now().minusDays(1));
        slot.setIsAvailable(true);

        when(itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId))
                .thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () ->
                availabilityService.setAvailability(itemId, ownerId, List.of(slot)));
    }
}
