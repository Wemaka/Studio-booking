package org.example.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Центральная OpenAPI-конфигурация контракта Studio Booking API.
 * Это НЕ Spring-бин. Здесь нет @Component/@Configuration.
 * springdoc-openapi подхватит @OpenAPIDefinition из jar.
 */
@OpenAPIDefinition(
		info = @Info(
				title = "Studio Booking API",
				version = "1.0.0",
				description = "REST контракт для сервиса бронирования студий записи",
				contact = @Contact(name = "Studio Booking Team", email = "studio@example.com")
		),
		servers = {
				@Server(url = "http://localhost:8080", description = "Local dev")
		},
		security = {
				@SecurityRequirement(name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER)
		},
		tags = {
				@Tag(name = "Studios", description = "Управление студиями (только чтение)"),
				@Tag(name = "Bookings", description = "Управление бронированиями студий")
		}
)
@SecurityScheme(
		name = StudioBookingApiConfig.SECURITY_SCHEME_BEARER,
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT",
		description = "Bearer токен (для учебного проекта может быть заглушкой)"
)
public final class StudioBookingApiConfig {
	private StudioBookingApiConfig() {
	}

	public static final String SECURITY_SCHEME_BEARER = "bearerAuth";
}
