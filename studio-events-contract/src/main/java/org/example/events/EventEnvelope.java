package org.example.events;

public record EventEnvelope<T>(
		EventMetadata metadata,
		T payload
) {
	public static <T> EventEnvelope<T> wrap(T payload, String source, String eventType) {
		return new EventEnvelope<>(
				EventMetadata.create(source, eventType),
				payload
		);
	}
}
