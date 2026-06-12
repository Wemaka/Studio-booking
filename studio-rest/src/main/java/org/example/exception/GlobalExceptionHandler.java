package org.example.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.example.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Централизованная обработка исключений.
 * <p>Преобразует исключения в единообразный ответ {@link ErrorResponse}.</p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final String BASE_PROBLEM_URI = "https://api.example.com/problems/";

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(NotFoundException ex,
	                                                            HttpServletRequest req) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse(
						HttpStatus.NOT_FOUND.value(),
						BASE_PROBLEM_URI + "resource-not-found",
						"Ресурс не найден",
						ex.getMessage(),
						req.getRequestURI(),
						Instant.now(),
						null
				));
	}

	// Пример кастомного исключения для конфликта (если нужно)
	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex,
	                                                    HttpServletRequest req) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(new ErrorResponse(
						HttpStatus.CONFLICT.value(),
						BASE_PROBLEM_URI + "conflict",
						"Конфликт данных",
						ex.getMessage(),
						req.getRequestURI(),
						Instant.now(),
						null
				));
	}

	/**
	 * Обработка ошибки валидации @Valid в контроллерах (например, для WorkingHoursRequest).
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
	                                                      HttpServletRequest req) {
		List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();

		// Ошибки конкретных полей (@NotBlank, @Size и т.д.)
		ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> new ErrorResponse.FieldError(
						fe.getField(),
						fe.getRejectedValue(),
						fe.getDefaultMessage()
				))
				.forEach(fieldErrors::add);

		// Ошибки уровня класса (@ValidWorkingHours и другие class-level аннотации)
		ex.getBindingResult().getGlobalErrors().stream()
				.map(ge -> new ErrorResponse.FieldError(
						ge.getObjectName(),
						null,
						ge.getDefaultMessage()
				))
				.forEach(fieldErrors::add);

		String detail = fieldErrors.stream()
				.map(fe -> fe.field() + ": " + fe.message())
				.collect(Collectors.joining("; "));

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(
						HttpStatus.BAD_REQUEST.value(),
						BASE_PROBLEM_URI + "validation-error",
						"Ошибка валидации входных данных",
						detail,
						req.getRequestURI(),
						Instant.now(),
						fieldErrors
				));
	}

	/**
	 * Обработка нарушений валидации (например, от WorkingHoursValidator).
	 * Возникает при использовании @Validated на уровне метода или при ручной валидации.
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
	                                                               HttpServletRequest req) {
		List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
				.map(violation -> new ErrorResponse.FieldError(
						extractPropertyPath(violation),
						violation.getInvalidValue(),
						violation.getMessage()
				))
				.toList();

		String detail = fieldErrors.stream()
				.map(fe -> fe.field() + ": " + fe.message())
				.collect(Collectors.joining("; "));

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(
						HttpStatus.BAD_REQUEST.value(),
						BASE_PROBLEM_URI + "constraint-violation",
						"Ошибка валидации",
						detail,
						req.getRequestURI(),
						Instant.now(),
						fieldErrors
				));
	}

	/**
	 * Обрабатывает все непредвиденные исключения.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
		// Здесь добавьте логирование ошибки: log.error("Unexpected error", ex);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(
						HttpStatus.INTERNAL_SERVER_ERROR.value(),
						BASE_PROBLEM_URI + "internal-error",
						"Внутренняя ошибка сервера",
						"Произошла непредвиденная ошибка. Пожалуйста, обратитесь в поддержку.",
						req.getRequestURI(),
						Instant.now(),
						null
				));
	}

	private String extractPropertyPath(ConstraintViolation<?> violation) {
		String path = violation.getPropertyPath().toString();
		// Путь может быть "methodName.fieldName" или просто "fieldName"
		int lastDot = path.lastIndexOf('.');
		return lastDot == -1 ? path : path.substring(lastDot + 1);
	}
}
