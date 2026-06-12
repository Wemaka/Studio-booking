package org.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

@Schema(description = "Полное обновление студии")
public record UpdateStudioRequest(
		@NotBlank(message = "Имя не может быть пустым")
		@Size(min = 3, max = 100, message = "Длина имени должна быть 3-100 символов")
		@Schema(description = "Название студии", example = "Studio A - Vocal Room", requiredMode = Schema.RequiredMode.REQUIRED)
		String name,

		@NotBlank(message = "Местоположение не может быть пустым")
		@Size(max = 200, message = "Местоположение не может превышать 200 символов")
		@Schema(description = "Местоположение студии", example = "Москва, ул. Пушкина, 10")
		String location,

		@NotNull(message = "Рабочие часы не могут быть пустыми")
		@Schema(description = "Начало времени работы", example = "09:00")
		String workingHoursStart,

		@NotNull(message = "Рабочие часы не могут быть пустыми")
		@Schema(description = "Конец времени работы", example = "21:00")
		String workingHoursEnd,

		@NotNull(message = "Доступность не может быть пустой")
		@Schema(description = "Доступность для бронирования", example = "true")
		Boolean isActive,

		@NotNull(message = "Цена в час не может быть пустой")
		@Schema(description = "Цена студии за час бронирования", example = "1")
		Integer pricePerHour
) {
}