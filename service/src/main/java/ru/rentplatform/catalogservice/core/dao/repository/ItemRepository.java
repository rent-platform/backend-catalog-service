package ru.rentplatform.catalogservice.core.dao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    Optional<Item> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Item> findByIdAndOwnerIdAndDeletedAtIsNull(UUID id, UUID ownerId);

    Page<Item> findAllByDeletedAtIsNullAndStatus(ItemStatus status, Pageable pageable);

    Page<Item> findAllByOwnerIdAndDeletedAtIsNull(UUID ownerId, Pageable pageable);

    Page<Item> findAllByOwnerIdAndDeletedAtIsNullAndStatus(UUID ownerId, ItemStatus status, Pageable pageable);

    @Query("""
        SELECT i FROM Item i
        WHERE i.deletedAt IS NULL
          AND i.status = :status
          AND (:categoryId IS NULL OR i.category.id = :categoryId)
          AND (:city = '' OR LOWER(i.city) = :city)
          AND (:searchQuery = '' OR LOWER(i.title) LIKE CONCAT('%', :searchQuery, '%'))
          AND (:minPricePerDay IS NULL OR i.pricePerDay >= :minPricePerDay)
          AND (:maxPricePerDay IS NULL OR i.pricePerDay <= :maxPricePerDay)
          AND (:minPricePerHour IS NULL OR i.pricePerHour >= :minPricePerHour)
          AND (:maxPricePerHour IS NULL OR i.pricePerHour <= :maxPricePerHour)
        """)
    Page<Item> searchActiveItems(
            @Param("status") ItemStatus status,
            @Param("categoryId") Long categoryId,
            @Param("city") String city,
            @Param("searchQuery") String searchQuery,
            @Param("minPricePerDay") BigDecimal minPricePerDay,
            @Param("maxPricePerDay") BigDecimal maxPricePerDay,
            @Param("minPricePerHour") BigDecimal minPricePerHour,
            @Param("maxPricePerHour") BigDecimal maxPricePerHour,
            Pageable pageable
    );

    @Query("""
        SELECT i FROM Item i
        WHERE i.deletedAt IS NULL
          AND i.status = :status
          AND i.id <> :itemId
          AND i.category.id = :categoryId
          AND LOWER(i.city) = LOWER(:city)
        """)
    Page<Item> findSimilarItems(
            @Param("itemId") UUID itemId,
            @Param("categoryId") Long categoryId,
            @Param("city") String city,
            @Param("status") ItemStatus status,
            Pageable pageable
    );
}