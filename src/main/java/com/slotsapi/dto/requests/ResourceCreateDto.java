package com.slotsapi.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Тело запроса для создания нового ресурса")
public class ResourceCreateDto {

    @Schema(description = "Название или имя ресурса", example = "Доктор Ковалев (Радиолог)", required = true)
    private String name;

    @Schema(description = "Категория ресурса", example = "STAFF", required = true)
    private String type;

    @Schema(description = "Локальная таймзона объекта (IANA ID)", example = "Europe/Moscow", defaultValue = "UTC")
    private String timezone;
}
