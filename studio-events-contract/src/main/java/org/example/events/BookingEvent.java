package org.example.events;

import java.time.OffsetDateTime;

public sealed interface BookingEvent {

	record Created(
			Long bookingId,
			String clientId,
			Long studioId,
			OffsetDateTime startTime,
			Integer durationMinutes,
			String clientNotes
	) implements BookingEvent {}

	record Updated(
			Long bookingId,
			Long studioId,
			OffsetDateTime startTime,
			Integer durationMinutes,
			String status,
			String clientNotes
	) implements BookingEvent {}

	record Cancelled(
			Long bookingId
	) implements BookingEvent {}
}
