package ru.rentplatform.catalogservice.core.dao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "availability")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Availability {

    @EmbeddedId
    private AvailabilityId id;

    @MapsId("itemId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
}
