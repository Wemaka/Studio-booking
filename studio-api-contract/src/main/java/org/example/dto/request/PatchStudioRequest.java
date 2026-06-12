package org.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Частичное обновление студии")
public record PatchStudioRequest(
		@Size(min = 3, max = 100, message = "Длина имени должна быть 3-100 символов")
		@Schema(description = "Название студии", example = "Studio A - Vocal Room")
		String name,

		@Size(min = 3, max = 200, message = "Название должно быть 3-200 символов")
		@Schema(description = "Местоположение студии", example = "Москва, ул. Пушкина, 10")
		String location,

		@Schema(description = "Начало времени работы", example = "09:00")
		String workingHoursStart,

		@Schema(description = "Конец времени работы", example = "21:00")
		String workingHoursEnd,

		@Schema(description = "Доступность для бронирования", example = "true")
		Boolean isActive,

		@Schema(description = "Цена студии за час бронирования", example = "1")
		@Min(1)
		Integer pricePerHour
) {
}
