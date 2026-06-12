package org.example.graphql.types.input;

import java.time.OffsetDateTime;

public record CreateBookingInputGql(
		String clientId,
		Long studioId,
		OffsetDateTime startTime,
		Integer durationMinutes,
		String clientNotes
) {
}
