package ru.rentplatform.catalogservice.core.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.rentplatform.catalogservice.api.dto.response.CategoryResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemResponse;
import ru.rentplatform.catalogservice.api.dto.response.ItemShortResponse;
import ru.rentplatform.catalogservice.api.dto.response.PhotoResponse;
import ru.rentplatform.catalogservice.core.dao.entity.Category;
import ru.rentplatform.catalogservice.core.dao.entity.Item;
import ru.rentplatform.catalogservice.core.dao.entity.Photo;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-25T15:11:50+0500",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.4.1.jar, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class CatalogMapperImpl implements CatalogMapper {

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.parentId( categoryParentId( category ) );
        categoryResponse.id( category.getId() );
        categoryResponse.categoryName( category.getCategoryName() );
        categoryResponse.slug( category.getSlug() );
        categoryResponse.sortOrder( category.getSortOrder() );
        categoryResponse.isActive( category.getIsActive() );

        return categoryResponse.build();
    }

    @Override
    public PhotoResponse toPhotoResponse(Photo photo) {
        if ( photo == null ) {
            return null;
        }

        PhotoResponse.PhotoResponseBuilder photoResponse = PhotoResponse.builder();

        photoResponse.id( photo.getId() );
        photoResponse.photoUrl( photo.getPhotoUrl() );
        photoResponse.sortOrder( photo.getSortOrder() );

        return photoResponse.build();
    }

    @Override
    public ItemResponse toItemResponse(Item item) {
        if ( item == null ) {
            return null;
        }

        ItemResponse.ItemResponseBuilder itemResponse = ItemResponse.builder();

        itemResponse.id( item.getId() );
        itemResponse.ownerId( item.getOwnerId() );
        itemResponse.title( item.getTitle() );
        itemResponse.itemDescription( item.getItemDescription() );
        itemResponse.pricePerDay( item.getPricePerDay() );
        itemResponse.pricePerHour( item.getPricePerHour() );
        itemResponse.depositAmount( item.getDepositAmount() );
        itemResponse.city( item.getCity() );
        itemResponse.pickupLocation( item.getPickupLocation() );
        itemResponse.moderationComment( item.getModerationComment() );
        itemResponse.viewsCount( item.getViewsCount() );
        itemResponse.createdAt( item.getCreatedAt() );
        itemResponse.updatedAt( item.getUpdatedAt() );

        itemResponse.category( toCategoryResponse(item.getCategory()) );
        itemResponse.status( item.getStatus().name() );

        return itemResponse.build();
    }

    @Override
    public ItemShortResponse toItemShortResponse(Item item) {
        if ( item == null ) {
            return null;
        }

        ItemShortResponse.ItemShortResponseBuilder itemShortResponse = ItemShortResponse.builder();

        itemShortResponse.id( item.getId() );
        itemShortResponse.title( item.getTitle() );
        itemShortResponse.city( item.getCity() );
        itemShortResponse.pricePerDay( item.getPricePerDay() );
        itemShortResponse.pricePerHour( item.getPricePerHour() );

        itemShortResponse.status( item.getStatus().name() );

        return itemShortResponse.build();
    }

    private Long categoryParentId(Category category) {
        Category parent = category.getParent();
        if ( parent == null ) {
            return null;
        }
        return parent.getId();
    }
}
