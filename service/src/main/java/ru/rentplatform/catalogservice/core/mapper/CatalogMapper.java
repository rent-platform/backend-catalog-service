package ru.rentplatform.catalogservice.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.rentplatform.catalogservice.api.dto.response.CategoryResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.api.dto.response.PhotoResponse;
import ru.rentplatform.catalogservice.core.dao.entity.Category;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.Photo;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CatalogMapper {

    @Mapping(target = "parentId", source = "parent.id")
    CategoryResponse toCategoryResponse(Category category);

    PhotoResponse toPhotoResponse(Photo photo);

    @Mapping(target = "category", expression = "java(toCategoryResponse(item.getCategory()))")
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "status", expression = "java(item.getStatus().name())")
    ItemResponse toItemResponse(Item item);

    @Mapping(target = "mainPhotoUrl", ignore = true)
    @Mapping(target = "status", expression = "java(item.getStatus().name())")
    ItemShortResponse toItemShortResponse(Item item);
}