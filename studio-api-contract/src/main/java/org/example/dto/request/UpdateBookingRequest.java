package org.example.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.example.dto.enums.BookingStatus;

import java.time.OffsetDateTime;

@Schema(description = "Запрос на обновление (PUT)")
public record UpdateBookingRequest(
		@NotNull(message = "Время не может быть пустым")
		@FutureOrPresent
		@Schema(description = "Время начала сессии")
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
		OffsetDateTime startTime,

		@NotNull(message = "Длительность не может быть пустым")
		@Min(30) @Max(480)
		@Schema(description = "Длительность в минутах (30–480)", example = "120")
		Integer durationMinutes,

		@NotNull(message = "Статус не может быть пустым")
		@Schema(description = "Статус брони", example = "PENDING")
		BookingStatus status,

		@Size(max = 200, message = "Комментарий не может превышать 200 символов")
		@Schema(description = "Комментарий клиента")
		String clientNotes
) {
}
