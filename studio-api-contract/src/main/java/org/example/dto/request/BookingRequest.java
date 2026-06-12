package org.example.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

@Schema(description = "Запрос на создание брони")
public record BookingRequest(
		@NotBlank(message = "ID клиента не может быть пустым")
		@Schema(description = "ID клиента (генерируется на фронте)", example = "client-uuid-123")
		String clientId,

		@NotNull(message = "ID студии не может быть пустым")
		@Schema(description = "ID студии", example = "1")
		Long studioId,

		@NotNull(message = "Время не может быть пустым")
		@FutureOrPresent
		@Schema(description = "Время начала сессии")
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
		OffsetDateTime startTime,

		@NotNull(message = "Длительность не может быть пустым")
		@Min(30) @Max(480)
		@Schema(description = "Длительность в минутах (30–480)", example = "120")
		Integer durationMinutes,

		@Size(max = 200, message = "Комментарий не может превышать 200 символов")
		@Schema(description = "Комментарий клиента")
		String clientNotes
) {
}
