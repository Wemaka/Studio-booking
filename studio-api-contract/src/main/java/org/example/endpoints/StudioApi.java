package org.example.endpoints;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.config.StudioBookingApiConfig;
import org.example.dto.request.PatchStudioRequest;
import org.example.dto.request.StudioRequest;
import org.example.dto.request.UpdateStudioRequest;
import org.example.dto.response.AvailableSlotResponse;
import org.example.dto.response.ErrorResponse;
import org.example.dto.response.StudioResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Tag(name = "Studios", description = "Чтение информации о студиях и доступных слотах")
@RequestMapping(
		value = "/api/studios",
		produces = MediaType.APPLICATION_JSON_VALUE
)
public interface StudioApi {
	@Operation(
			summary = "Получить студию по ID",
			security = @SecurityRequirement(name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER)
	)
	@ApiResponse(responseCode = "200", description = "Студия найдена")
	@ApiResponse(responseCode = "404", description = "Студия не найдена",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping("/{id}")
	EntityModel<StudioResponse> getStudioById(
			@Parameter(description = "ID студии", required = true, example = "1") @PathVariable Long id
	);

	@Operation(
			summary = "Список студий",
			description = "Возвращает список всех студий.",
			security = @SecurityRequirement(name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER)
	)
	@ApiResponse(responseCode = "200", description = "Список студий")
	@GetMapping
	PagedModel<EntityModel<StudioResponse>> getAllStudios(
			@Parameter(description = "Поиск по названию студии") @RequestParam(required = false) String name,
			@Parameter(description = "Поиска по адресу студии") @RequestParam(required = false) String location,
			@Parameter(description = "Фильтр по цене студии") @RequestParam(required = false) Integer pricePerHour,
			@Parameter(description = "Фильтр по доступности") @RequestParam(required = false) Boolean isActive,
			@Parameter(description = "Номер страницы (0..N)", example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
	);

	@Operation(
			summary = "Доступные слоты студии",
			description = "Возвращает список свободных слотов с рассчитанной ценой.",
			security = @SecurityRequirement(name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER)
	)
	@ApiResponse(responseCode = "200", description = "Список доступных слотов")
	@ApiResponse(responseCode = "400", description = "Ошибка валидации фильтра",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping(value = "/available-slots")
	PagedModel<EntityModel<AvailableSlotResponse>> getAllAvailableSlots(
			@Parameter(description = "Фильтр по ID студии")
			@RequestParam(required = false) Long studioId,

			@Parameter(description = "Дата поиска (ISO-8601)", example = "2026-05-20")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

			@Parameter(description = "Точное время начала (ISO-8601)", example = "15:00:00")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,

			@Parameter(description = "Продолжительность в минутах", example = "60")
			@RequestParam(required = false, defaultValue = "60") Integer durationMinutes,
			
			@Parameter(description = "Номер страницы (0..N)", example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
	);

	@Operation(summary = "Создать новую студию")
	@ApiResponse(responseCode = "201", description = "Студия создана")
	@ApiResponse(responseCode = "400", description = "Ошибка валидации",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	ResponseEntity<EntityModel<StudioResponse>> createStudio(@Valid @RequestBody StudioRequest request);

	@Operation(summary = "Обновить студию",
			description = "Полное обновление (JSON Merge Patch)")
	@ApiResponse(responseCode = "200", description = "Студия обновлена")
	@ApiResponse(responseCode = "400", description = "Ошибка валидации",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Студия не найдена",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	EntityModel<StudioResponse> updateStudio(
			@Parameter(description = "ID студии", example = "1") @PathVariable Long id,
			@Valid @RequestBody UpdateStudioRequest request
	);

	@Operation(summary = "Обновить студию",
			description = "Частичное обновление (JSON Merge Patch)")
	@ApiResponse(responseCode = "200", description = "Студия обновлена")
	@ApiResponse(responseCode = "400", description = "Ошибка валидации",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Студия не найдена",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	EntityModel<StudioResponse> patchStudio(
			@Parameter(description = "ID студии", example = "1") @PathVariable Long id,
			@Valid @RequestBody PatchStudioRequest request
	);
}
