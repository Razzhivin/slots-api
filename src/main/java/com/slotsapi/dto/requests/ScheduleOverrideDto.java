package com.slotsapi.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Schema(description = "Тело запроса для массовой перезаписи расписания")
public class ScheduleOverrideDto {

    @Schema(description = "Массив рабочих интервалов")
    @NotNull(message = "Intervals list cannot be null.")
    @NotEmpty(message = "Intervals list cannot be empty.")
    @Valid
    private List<IntervalCreateDto> intervals;
}
