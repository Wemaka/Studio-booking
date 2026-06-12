package org.example.events;

import java.time.LocalTime;

public sealed interface StudioEvent {
	record Created(
			Long studioId,
			String name,
			String location,
			LocalTime workingHoursStart,
			LocalTime workingHoursEnd,
			Integer pricePerHour
	) implements StudioEvent {}

	record Updated(
			Long studioId,
			String name,
			String location,
			LocalTime workingHoursStart,
			LocalTime workingHoursEnd,
			Boolean isActive,
			Integer pricePerHour
	) implements StudioEvent {}

	// Обогащенное событие на основе gRPC-ответа аналитики
	record Enriched(
			Long studioId,
			String name,
			double occupancyRate,
			double totalRevenue,
			double avgBookingDuration,
			double recommendationScore
	) implements StudioEvent {}
}
