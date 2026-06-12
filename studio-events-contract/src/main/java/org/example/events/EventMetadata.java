package org.example.events;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EventMetadata(
		String eventId,
		Instant timestamp,
		String sourceService,
		String eventType
) {
	public static EventMetadata create(String sourceService, String eventType) {
		return new EventMetadata(
				UUID.randomUUID().toString(),
				Instant.now(),
				sourceService,
				eventType
		);
	}
}
