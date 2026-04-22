package ru.rentplatform.catalogservice.core.dao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentplatform.catalogservice.core.dao.entity.FavoriteItem;
import ru.rentplatform.catalogservice.core.dao.entity.FavoriteItemId;

import java.util.UUID;

public interface FavoriteItemRepository extends JpaRepository<FavoriteItem, FavoriteItemId> {

    boolean existsByIdUserIdAndIdItemId(UUID userId, UUID itemId);

    void deleteByIdUserIdAndIdItemId(UUID userId, UUID itemId);

    Page<FavoriteItem> findAllByIdUserId(UUID userId, Pageable pageable);
}