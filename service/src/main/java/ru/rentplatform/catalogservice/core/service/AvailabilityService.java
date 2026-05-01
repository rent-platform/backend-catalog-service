package ru.rentplatform.catalogservice.core.service;

import ru.rentplatform.catalogservice.api.dto.request.AvailabilityRequest;
import ru.rentplatform.catalogservice.api.dto.response.AvailabilityResponse;
import ru.rentplatform.catalogservice.api.dto.response.MessageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AvailabilityService {

    List<AvailabilityResponse> getAvailability(UUID itemId, LocalDate startDate, LocalDate endDate, UUID currentUserId);

    List<AvailabilityResponse> setAvailability(UUID itemId, UUID ownerId, List<AvailabilityRequest.AvailabilitySlot> slots);

    MessageResponse deleteAvailability(UUID itemId, UUID ownerId, LocalDate startDate, LocalDate endDate);
}