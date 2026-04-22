package ru.rentplatform.catalogservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectItemRequest {

    @NotBlank
    @Size(max = 1000)
    private String moderationComment;
}
