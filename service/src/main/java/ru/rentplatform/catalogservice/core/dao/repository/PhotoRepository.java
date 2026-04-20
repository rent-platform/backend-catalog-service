package ru.rentplatform.catalogservice.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentplatform.catalogservice.core.dao.entity.Photo;

import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {

    List<Photo> findAllByItem_IdOrderBySortOrderAsc(UUID itemId);

    void deleteAllByItem_Id(UUID itemId);
}