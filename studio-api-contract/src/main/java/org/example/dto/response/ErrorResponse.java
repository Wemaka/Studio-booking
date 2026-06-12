package org.example.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Стандартный ответ об ошибке (RFC 7807 Problem Details)")
public record ErrorResponse(

		@Schema(description = "HTTP статус-код", example = "404")
		int status,

		@Schema(
				description = "URI-идентификатор типа ошибки. Машиночитаемый; клиент может switch по нему.",
				example = "https://api.example.com/problems/resource-not-found"
		)
		String type,

		@Schema(description = "Краткое человекочитаемое название типа ошибки", example = "Ресурс не найден")
		String title,

		@Schema(description = "Детальное описание конкретного случая ошибки", example = "Книга с id=42 не существует")
		String detail,

		@Schema(description = "URI запроса, приведшего к ошибке", example = "/api/books/42")
		String instance,

		@Schema(description = "Момент возникновения ошибки (UTC)", example = "2026-03-03T10:15:30Z")
		Instant timestamp,

		@Schema(description = "Ошибки по отдельным полям (заполняется только для 400 Bad Request с ошибками валидации)")
		List<FieldError> fieldErrors
) {

	/**
	 * Ошибка валидации конкретного поля запроса.
	 */
	@Schema(description = "Ошибка валидации поля")
	public record FieldError(

			@Schema(description = "Имя невалидного поля", example = "isbn")
			String field,

			@Schema(description = "Значение, которое было отклонено", example = "123")
			Object rejectedValue,

			@Schema(description = "Причина отклонения", example = "Некорректный ISBN. Допустимые форматы: ISBN-10 или ISBN-13")
			String message
	) {}
}
