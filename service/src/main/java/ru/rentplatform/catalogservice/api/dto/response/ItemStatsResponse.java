package ru.rentplatform.catalogservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Статистика объявления для владельца")
public class ItemStatsResponse {

    @Schema(description = "ID объявления")
    private java.util.UUID itemId;

    @Schema(description = "Заголовок объявления")
    private String title;

    @Schema(description = "Количество просмотров", example = "156")
    private int viewsCount;

    @Schema(description = "Текущий статус объявления")
    private String status;
}
