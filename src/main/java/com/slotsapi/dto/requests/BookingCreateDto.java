package com.slotsapi.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@Schema(description = "Тело запроса для создания бронирования")
public class BookingCreateDto {

    @Schema(description = "Список уникальных идентификаторов ресурсов (цепочка)", example = "[1, 2, 3]", required = true)
    @NotNull(message = "Resource IDs list cannot be null.")
    @NotEmpty(message = "Resource IDs list cannot be empty.")
    private Set<Long> resourceIds;

    @NotBlank(message = "Start time cannot be empty.")
    @NotNull(message = "Start time cannot be null.")
    @Schema(description = "Время начала бронирования в UTC формате ISO-8601", example = "2026-08-24T07:00:00Z", required = true)
    private String startTime;

    @NotBlank(message = "End time cannot be empty.")
    @NotNull(message = "End time cannot be null.")
    @Schema(description = "Время окончания бронирования в UTC формате ISO-8601", example = "2026-08-24T08:00:00Z", required = true)
    private String endTime;
}
