package ru.rentplatform.catalogservice.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentplatform.catalogservice.core.dao.entity.Availability;
import ru.rentplatform.catalogservice.core.dao.entity.AvailabilityId;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AvailabilityRepository extends JpaRepository<Availability, AvailabilityId> {

    List<Availability> findAllByItem_IdOrderByIdAvailableDateAsc(UUID itemId);

    List<Availability> findAllByIdAvailableDateAndIsAvailableTrue(LocalDate availableDate);

    void deleteAllByItem_Id(UUID itemId);
}
