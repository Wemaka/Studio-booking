package org.example.storage;

import jakarta.annotation.PostConstruct;
import org.example.dto.enums.BookingStatus;
import org.example.dto.response.BookingResponse;
import org.example.dto.response.StudioResponse;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStorage {
	public final Map<Long, StudioResponse> studios = new ConcurrentHashMap<>();
	public final Map<Long, BookingResponse> bookings = new ConcurrentHashMap<>();

	public final AtomicLong studioSequence = new AtomicLong(0);
	public final AtomicLong bookingSequence = new AtomicLong(0);

	@PostConstruct
	public void init() {
		StudioResponse studio1 = StudioResponse.builder()
				.id(studioSequence.incrementAndGet())
				.name("Studio One")
				.location("Арбат, дом 23 Moscow, Moscow, Russia 119002")
				.workingHoursStart("09:00")
				.workingHoursEnd("21:00")
				.isActive(true)
				.pricePerHour(1500)
				.build();

		StudioResponse studio2 = StudioResponse.builder()
				.id(studioSequence.incrementAndGet())
				.name("Studio Two")
				.location("663733, Омская область, город Орехово-Зуево, наб. Домодедовская, 22")
				.workingHoursStart("08:00")
				.workingHoursEnd("20:00")
				.isActive(true)
				.pricePerHour(500)
				.build();

		studios.put(studio1.getId(), studio1);
		studios.put(studio2.getId(), studio2);

		long bookingId1 = bookingSequence.incrementAndGet();
		bookings.put(bookingId1, BookingResponse.builder()
				.id(bookingId1)
				.clientId("client-uuid-123")
				.studio(studio1)
				.startTime(makeBookingTime(1, 10, 0)) // завтра 10:00
				.durationMinutes(60)
				.status(BookingStatus.CONFIRMED)
				.clientNotes("very good")
				.build()
		);

		long bookingId2 = bookingSequence.incrementAndGet();
		bookings.put(bookingId2, BookingResponse.builder()
				.id(bookingId2)
				.clientId("client-uuid-456")
				.studio(studio2)
				.startTime(makeBookingTime(0, 15, 0)) // сегодня 15:00
				.durationMinutes(120)
				.status(BookingStatus.PENDING)
				.clientNotes(null)
				.build()
		);
	}

	private OffsetDateTime makeBookingTime(int plusDays, int hour, int minute) {
		return OffsetDateTime.now()
				.plusDays(plusDays)
				.withHour(hour)
				.withMinute(minute)
				.withSecond(0)
				.withNano(0);
	}
}
