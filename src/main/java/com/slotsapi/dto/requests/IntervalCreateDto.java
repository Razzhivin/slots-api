package com.slotsapi.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Тело запроса для добавления рабочего интервала")
public class IntervalCreateDto {

    @Schema(description = "День недели", example = "MONDAY", allowableValues = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}, required = true)
    @NotNull(message = "Day of week cannot be null.")
    @NotBlank(message = "Day of week cannot be empty.")
    private String dayOfWeek;

    @Schema(description = "Время начала работы (ЧЧ:ММ:СС)", example = "09:00:00", required = true)
    @NotNull(message = "Start time cannot be null.")
    @NotBlank(message = "Start time cannot be empty.")
    private String startTime;

    @Schema(description = "Время окончания работы (ЧЧ:ММ:СС)", example = "13:00:00", required = true)
    @NotNull(message = "End time cannot be null.")
    @NotBlank(message = "End time cannot be empty.")
    private String endTime;
}
