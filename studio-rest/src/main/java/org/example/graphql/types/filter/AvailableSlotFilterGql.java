package org.example.graphql.types.filter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record AvailableSlotFilterGql(
		Long studioId,
		LocalDate date,
		LocalTime time,
		Integer durationMinutes
) {
}
