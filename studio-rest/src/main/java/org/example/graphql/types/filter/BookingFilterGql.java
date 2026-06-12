package org.example.graphql.types.filter;

import org.example.dto.enums.BookingStatus;

import java.time.OffsetDateTime;

public record BookingFilterGql(
		Long clientId,
		Long studioId,
		OffsetDateTime startTime,
		Integer durationMinutes,
		BookingStatus status
) {
}
