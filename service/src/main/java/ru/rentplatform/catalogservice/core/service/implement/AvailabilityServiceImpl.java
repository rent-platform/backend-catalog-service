package ru.rentplatform.catalogservice.core.service.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentplatform.catalogservice.api.dto.request.AvailabilityRequest;
import ru.rentplatform.catalogservice.api.dto.response.AvailabilityResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;
import ru.rentplatform.catalogservice.api.exception.AccessDeniedException;
import ru.rentplatform.catalogservice.api.exception.InvalidItemStatusException;
import ru.rentplatform.catalogservice.api.exception.ItemNotFoundException;
import ru.rentplatform.catalogservice.core.dao.entity.Availability;
import ru.rentplatform.catalogservice.core.dao.entity.AvailabilityId;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;
import ru.rentplatform.catalogservice.core.dao.repository.AvailabilityRepository;
import ru.rentplatform.catalogservice.core.dao.repository.ItemRepository;
import ru.rentplatform.catalogservice.core.mapper.CatalogMapper;
import ru.rentplatform.catalogservice.core.service.AvailabilityService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final List<ItemStatus> ALLOWED_STATUSES = List.of(ItemStatus.DRAFT, ItemStatus.ACTIVE);

    private final AvailabilityRepository availabilityRepository;
    private final ItemRepository itemRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailability(UUID itemId, LocalDate startDate, LocalDate endDate, UUID currentUserId) {
        Item item = itemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        boolean isOwner = currentUserId != null && currentUserId.equals(item.getOwnerId());

        if (!isOwner && item.getStatus() != ItemStatus.ACTIVE) {
            return List.of();
        }

        List<Availability> availabilities = availabilityRepository
                .findByItemIdAndDateRange(itemId, startDate, endDate);

        return catalogMapper.toAvailabilityResponseList(availabilities);
    }

    @Override
    @Transactional
    public List<AvailabilityResponse> setAvailability(UUID itemId, UUID ownerId, List<AvailabilityRequest.AvailabilitySlot> slots) {
        Item item = itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId)
                .orElseThrow(() -> new AccessDeniedException("You are not allowed to modify this item"));

        validateItemStatus(item);

        List<Availability> entities = new ArrayList<>();
        for (AvailabilityRequest.AvailabilitySlot slot : slots) {
            if (slot.getDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Cannot set availability for past date: " + slot.getDate());
            }

            Optional<Availability> existing = availabilityRepository.findById(
                    AvailabilityId.builder()
                            .itemId(itemId)
                            .availableDate(slot.getDate())
                            .build()
            );

            if (existing.isPresent() && existing.get().getIsAvailable().equals(slot.getIsAvailable())) {
                throw new IllegalArgumentException(
                        String.format("Availability for date %s is already set to %s", slot.getDate(), slot.getIsAvailable())
                );
            }

            Availability availability = Availability.builder()
                    .id(AvailabilityId.builder()
                            .itemId(itemId)
                            .availableDate(slot.getDate())
                            .build())
                    .item(item)
                    .isAvailable(slot.getIsAvailable())
                    .build();

            entities.add(availability);
        }

        availabilityRepository.saveAll(entities);

        log.info("Availability updated for item {}: {} slots", itemId, slots.size());
        return catalogMapper.toAvailabilityResponseList(entities);
    }

    @Override
    @Transactional
    public MessageResponse deleteAvailability(UUID itemId, UUID ownerId, LocalDate startDate, LocalDate endDate) {
        Item item = itemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(itemId, ownerId)
                .orElseThrow(() -> new AccessDeniedException("You are not allowed to modify this item"));

        validateItemStatus(item);

        int deleted = availabilityRepository.deleteByItemIdAndDateRange(itemId, startDate, endDate);

        String message = String.format("Deleted %d availability records for item %s in range %s - %s",
                deleted, itemId, startDate, endDate);
        log.info(message);

        return new MessageResponse(message);
    }

    private void validateItemStatus(Item item) {
        if (!ALLOWED_STATUSES.contains(item.getStatus())) {
            throw new InvalidItemStatusException(
                    String.format("Cannot modify availability for item with status '%s'. Allowed statuses: %s",
                            item.getStatus(), ALLOWED_STATUSES)
            );
        }
    }
}