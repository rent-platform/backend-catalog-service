package ru.rentplatform.catalogservice.core.dao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.ItemStatus;

import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    Optional<Item> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Item> findByIdAndOwnerIdAndDeletedAtIsNull(UUID id, UUID ownerId);

    Page<Item> findAllByDeletedAtIsNullAndStatus(ItemStatus status, Pageable pageable);

    Page<Item> findAllByOwnerIdAndDeletedAtIsNull(UUID ownerId, Pageable pageable);

    Page<Item> findAllByCategoryIdAndDeletedAtIsNullAndStatus(Long categoryId, ItemStatus status, Pageable pageable);

    Page<Item> findAllByCityIgnoreCaseAndDeletedAtIsNullAndStatus(String city, ItemStatus status, Pageable pageable);

    Page<Item> findAllByTitleContainingIgnoreCaseAndDeletedAtIsNullAndStatus(String title, ItemStatus status, Pageable pageable);
}