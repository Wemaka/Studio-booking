package org.example.events;

public final class RoutingKeys {
	private RoutingKeys() {}

	public static final String EXCHANGE = "studio.events";
	// События студий
	public static final String STUDIO_CREATED = "studio.created";
	public static final String STUDIO_UPDATED = "studio.updated";
	public static final String STUDIO_ENRICHED = "studio.enriched";

	// События бронирования
	public static final String BOOKING_CREATED = "booking.created";
	public static final String BOOKING_UPDATED = "booking.updated";
	public static final String BOOKING_CANCELLED = "booking.cancelled";

	// Паттерны для подписки (wildcard)
	public static final String ALL_STUDIO_EVENTS = "studio.*";
	public static final String ALL_BOOKING_EVENTS = "booking.*";
	public static final String ALL_EVENTS = "#";
}
