package ru.rentplatform.catalogservice.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rentplatform.catalogservice.core.dao.entity.Availability;
import ru.rentplatform.catalogservice.core.dao.entity.AvailabilityId;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AvailabilityRepository extends JpaRepository<Availability, AvailabilityId> {

    List<Availability> findAllByItem_IdOrderByIdAvailableDateAsc(UUID itemId);

    List<Availability> findAllByIdAvailableDateAndIsAvailableTrue(LocalDate availableDate);

    void deleteAllByItem_Id(UUID itemId);

    @Query("""
        SELECT a FROM Availability a
        WHERE a.id.itemId = :itemId
          AND a.id.availableDate BETWEEN :startDate AND :endDate
        ORDER BY a.id.availableDate ASC
    """)
    List<Availability> findByItemIdAndDateRange(
            @Param("itemId") UUID itemId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Modifying
    @Query("""
        DELETE FROM Availability a
        WHERE a.id.itemId = :itemId
          AND a.id.availableDate BETWEEN :startDate AND :endDate
    """)
    int deleteByItemIdAndDateRange(
            @Param("itemId") UUID itemId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
