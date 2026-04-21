package ru.rentplatform.catalogservice.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentplatform.catalogservice.core.dao.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByIdAndDeletedAtIsNullAndIsActiveTrue(Long id);

    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    List<Category> findAllByDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc();

    List<Category> findAllByParentIsNullAndDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc();

    List<Category> findAllByParentIdAndDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc(Long parentId);

    Optional<Category> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNullAndIdNot(String slug, Long id);
}
