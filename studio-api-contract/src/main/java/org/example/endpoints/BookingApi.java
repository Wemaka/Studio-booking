package org.example.endpoints;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.config.StudioBookingApiConfig;
import org.example.dto.enums.BookingStatus;
import org.example.dto.request.BookingRequest;
import org.example.dto.request.PatchBookingRequest;
import org.example.dto.response.BookingResponse;
import org.example.dto.response.ErrorResponse;
import org.example.dto.request.UpdateBookingRequest;
import org.example.dto.response.StudioResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@Tag(name = "Bookings", description = "Управление бронированиями студий записи")
@RequestMapping(
		value = "/api/bookings",
		produces = MediaType.APPLICATION_JSON_VALUE
)
public interface BookingApi {

	@Operation(
			summary = "Получить бронь по ID",
			security = @SecurityRequirement(name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER)
	)
	@ApiResponse(responseCode = "200", description = "Бронь найдена")
	@ApiResponse(responseCode = "404", description = "Бронь не найдена",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping("/{id}")
	EntityModel<BookingResponse> getBookingById(
			@Parameter(description = "ID брони", example = "100")
			@PathVariable Long id
	);

	@Operation(
			summary = "Список броней",
			description = "Возвращает список всех броней.",
			security = @SecurityRequirement(name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER)
	)
	@ApiResponse(responseCode = "200", description = "Список броней")
	@GetMapping
	PagedModel<EntityModel<BookingResponse>> getAllBookings(
			@Parameter(description = "Фильтр по ID клиента") @RequestParam(required = false) Long clientId,
			@Parameter(description = "Фильтр по ID студии") @RequestParam(required = false) Long studioId,

			@Parameter(description = "Начало интервала (ISO-8601)", example = "2026-05-20T10:00:00Z")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,

			@Parameter(description = "Фильтр по продолжительности") @RequestParam(required = false) Integer durationMinutes,
			@Parameter(description = "Фильтр по статусу") @RequestParam(required = false) BookingStatus status,
			@Parameter(description = "Номер страницы (0..N)", example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
	);


	@Operation(
			summary = "Создать бронь студии",
			description = "Создаёт бронь в статусе PENDING, публикует событие в шину.",
			security = @SecurityRequirement(name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER)
	)
	@ApiResponse(responseCode = "201", description = "Бронь создана",
			content = @Content(schema = @Schema(implementation = BookingResponse.class)))
	@ApiResponse(responseCode = "400", description = "Ошибка валидации или слот недоступен",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Студия не найдена",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "409", description = "Конфликт с существующей бронью",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	ResponseEntity<EntityModel<BookingResponse>> createBooking(
			@Valid @RequestBody BookingRequest request
	);

	@Operation(
			summary = "Полное обновление бронь",
			description = "Полное обновление полей брони (время, длительность, жанр и т.д.).",
			security = @SecurityRequirement(name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER)
	)
	@ApiResponse(responseCode = "200", description = "Бронь обновлена")
	@ApiResponse(responseCode = "400", description = "Ошибка валидации",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Бронь не найдена",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "409", description = "Новый слот конфликтует с другой бронью",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	EntityModel<BookingResponse> updateBooking(
			@Parameter(description = "ID брони", example = "100")
			@PathVariable Long id,
			@Valid @RequestBody UpdateBookingRequest request
	);

	@Operation(
			summary = "Частичное обновление бронь",
			description = "Частичное обновление полей брони (время, длительность, жанр и т.д.).",
			security = @SecurityRequirement(name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER)
	)
	@ApiResponse(responseCode = "200", description = "Бронь обновлена")
	@ApiResponse(responseCode = "400", description = "Ошибка валидации",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Бронь не найдена",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "409", description = "Новый слот конфликтует с другой бронью",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	EntityModel<BookingResponse> patchBooking(
			@Parameter(description = "ID брони", example = "100")
			@PathVariable Long id,
			@Valid @RequestBody PatchBookingRequest request
	);

	@Operation(
			summary = "Отменить бронь",
			description = "Переводит статус в CANCELLED, публикует событие в шину.",
			security = @SecurityRequirement(name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER)
	)
	@ApiResponse(responseCode = "200", description = "Бронь отменена")
	@ApiResponse(responseCode = "400", description = "Нельзя отменить (уже началась или завершена)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Бронь не найдена",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping("/{id}/cancel")
	void cancelBooking(
			@Parameter(description = "ID брони", example = "100")
			@PathVariable Long id
	);
}
