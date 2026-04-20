package ru.rentplatform.catalogservice.core.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AvailabilityId implements Serializable {

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;
}
