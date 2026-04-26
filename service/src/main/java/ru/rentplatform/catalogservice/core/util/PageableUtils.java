package ru.rentplatform.catalogservice.core.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageableUtils {

    private static final Set<String> ALLOWED_ITEM_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "pricePerDay",
            "pricePerHour",
            "title",
            "city",
            "status",
            "viewsCount"
    );

    private PageableUtils() {
    }

    public static Pageable buildPageable(int page, int size, String sortBy, String sortDirection) {
        if (!ALLOWED_ITEM_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }

        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection)
                .orElse(Sort.Direction.DESC);

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
