package ru.rentplatform.catalogservice.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentplatform.catalogservice.core.dao.entity.Photo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {

    List<Photo> findAllByItem_IdOrderBySortOrderAsc(UUID itemId);

    Optional<Photo> findByIdAndItem_Id(UUID photoId, UUID itemId);

    boolean existsByItem_IdAndPhotoUrl(UUID itemId, String photoUrl);

    void deleteAllByItem_Id(UUID itemId);
}