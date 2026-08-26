package com.slotsapi.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Объект для обновления данных ресурса")
public class ResourceUpdateDto {

    @Schema(description = "Новое имя ресурса", example = "Доктор Ковалев (Радиолог)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    @Schema(description = "Новый тип ресурса", example = "STAFF", allowableValues = {"STAFF", "ROOM", "EQUIPMENT"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String type;

    @Schema(description = "Новая таймзона ресурса", example = "Europe/Moscow", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String timezone;
}
