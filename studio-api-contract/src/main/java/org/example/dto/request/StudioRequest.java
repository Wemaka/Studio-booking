package org.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.example.validation.ValidWorkingHours;

@Schema(description = "Запрос на создание студии")
@ValidWorkingHours
public record StudioRequest(
		@NotBlank(message = "Имя не может быть пустым")
		@Size(min = 3, max = 100, message = "Длина имени должна быть 3-100 символов")
		@Schema(description = "Название студии", example = "Studio A - Vocal Room", requiredMode = Schema.RequiredMode.REQUIRED)
		String name,

		@NotBlank(message = "Местоположение не может быть пустым")
		@Size(min = 3, max = 200, message = "Метоположение должно быть 3-200 символов")
		@Schema(description = "Местоположение студии", example = "Москва, ул. Пушкина, 10")
		String location,

		@NotNull(message = "Рабочие часы не могут быть пустыми")
		@Schema(description = "Начало времени работы", requiredMode = Schema.RequiredMode.REQUIRED)
		String workingHoursStart,

		@NotNull(message = "Рабочие часы не могут быть пустыми")
		@Schema(description = "Конец времени работы", requiredMode = Schema.RequiredMode.REQUIRED)
		String workingHoursEnd,

		@NotNull(message = "Доступность не может быть пустой")
		@Schema(description = "Доступность для бронирования", defaultValue = "true", example = "true")
		Boolean isActive,

		@NotNull(message = "Цена в час не может быть пустой")
		@Schema(description = "Цена студии за час бронирования", example = "1")
		@Min(1)
		Integer pricePerHour
) {
}
