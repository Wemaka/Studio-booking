package org.example.graphql.types.input;

import org.example.dto.enums.BookingStatus;

import java.time.OffsetDateTime;

public record UpdateBookingInputGql(
		OffsetDateTime startTime,
		Integer durationMinutes,
		BookingStatus status,
		String clientNotes
) {
}
