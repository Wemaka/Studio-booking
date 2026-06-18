package org.example.service;

import org.example.dto.enums.BookingStatus;
import org.example.dto.request.BookingRequest;
import org.example.dto.request.PatchBookingRequest;
import org.example.dto.request.UpdateBookingRequest;
import org.example.dto.response.BookingResponse;
import org.example.dto.response.PagedResponse;
import org.example.dto.response.StudioResponse;
import org.example.event.BookingEventPublisher;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.storage.InMemoryStorage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class BookingService {
	private final InMemoryStorage storage;
	private final StudioService studioService;
	private final BookingEventPublisher eventPublisher;

	public BookingService(
			InMemoryStorage storage,
			@Lazy
			StudioService studio, BookingEventPublisher eventPublisher
	) {
		this.storage = storage;
		this.studioService = studio;
		this.eventPublisher = eventPublisher;
	}

	public BookingResponse getBookingById(Long id) {
		return Optional.ofNullable(storage.bookings.get(id))
				.orElseThrow(() -> new NotFoundException("Booking", id.toString()));
	}

	public PagedResponse<BookingResponse> getAllBookings(
			Long clientId, Long studioId, OffsetDateTime startTime, Integer durationMinutes,
			BookingStatus status, int page, int size
	) {
		Stream<BookingResponse> stream = storage.bookings.values().stream();

		if (clientId != null) {
			stream = stream.filter(b -> b.getClientId().equals(clientId.toString()));
		}
		if (studioId != null) {
			stream = stream.filter(b -> b.getStudio().getId().equals(studioId));
		}
		if (startTime != null) {
			stream = stream.filter(b -> b.getStartTime().isEqual(startTime));
		}
		if (durationMinutes != null) {
			stream = stream.filter(b -> b.getDurationMinutes().equals(durationMinutes));
		}
		if (status != null) {
			stream = stream.filter(b -> b.getStatus() == status);
		}

		List<BookingResponse> allBookings = stream.toList();
		return createPagedResponse(allBookings, page, size);
	}

	public BookingResponse createBooking(BookingRequest request) {
		StudioResponse studio = studioService.getStudioById(request.studioId());

		OffsetDateTime requestStart = request.startTime();
		OffsetDateTime requestEnd   = requestStart.plusMinutes(request.durationMinutes());

// Проверяем пересечение с существующими активными бронями напрямую
		boolean hasConflict = storage.bookings.values().stream()
				.filter(b -> b.getStudio().getId().equals(request.studioId()))
				.filter(b -> b.getStatus() != BookingStatus.CANCELLED)
				.anyMatch(b -> {
					OffsetDateTime existingStart = b.getStartTime();
					OffsetDateTime existingEnd   = existingStart.plusMinutes(b.getDurationMinutes());
					// Пересечение: новый начинается до конца существующего
					//              И заканчивается после начала существующего
					return requestStart.toInstant().isBefore(existingEnd.toInstant())
							&& requestEnd.toInstant().isAfter(existingStart.toInstant());
				});

		if (hasConflict) {
			throw new ConflictException(
					"Студия " + studio.getName() + " уже забронирована на выбранное время"
			);
		}

		long id = storage.bookingSequence.incrementAndGet();
		BookingResponse booking = BookingResponse.builder()
				.id(id)
				.clientId(request.clientId())
				.studio(studio)
				.startTime(request.startTime())
				.durationMinutes(request.durationMinutes())
				.status(BookingStatus.PENDING)
				.clientNotes(request.clientNotes())
				.build();

		storage.bookings.put(id, booking);
		eventPublisher.publishCreated(booking);
		return booking;
	}

	public BookingResponse updateBooking(Long id, UpdateBookingRequest request) {
		BookingResponse existing = getBookingById(id);

		BookingResponse updated = BookingResponse.builder()
				.id(id)
				.clientId(existing.getClientId())
				.studio(existing.getStudio())
				.startTime(request.startTime())
				.durationMinutes(request.durationMinutes())
				.status(request.status())
				.clientNotes(request.clientNotes())
				.build();

		storage.bookings.put(id, updated);
		eventPublisher.publishUpdated(updated);
		return updated;
	}

	public BookingResponse patchBooking(Long id, PatchBookingRequest request) {
		BookingResponse existing = getBookingById(id);

		BookingResponse updated = BookingResponse.builder()
				.id(id)
				.clientId(existing.getClientId())
				.studio(existing.getStudio())
				.startTime(request.startTime() != null ? request.startTime() : existing.getStartTime())
				.durationMinutes(request.durationMinutes() != null ? request.durationMinutes() : existing.getDurationMinutes())
				.status(request.status() != null ? request.status() : existing.getStatus())
				.clientNotes(request.clientNotes() != null ? request.clientNotes() : existing.getClientNotes())
				.build();

		storage.bookings.put(id, updated);
		eventPublisher.publishUpdated(updated);
		return updated;
	}

	public void cancelBooking(Long id) {
		BookingResponse existing = getBookingById(id);

		if (existing.getStatus() == BookingStatus.CANCELLED) {
			throw new ValidationException("Бронь уже отменена");
		}

		BookingResponse cancelled = BookingResponse.builder()
				.id(existing.getId())
				.clientId(existing.getClientId())
				.studio(existing.getStudio())
				.startTime(existing.getStartTime())
				.durationMinutes(existing.getDurationMinutes())
				.status(BookingStatus.CANCELLED)
				.clientNotes(existing.getClientNotes())
				.build();

		storage.bookings.put(id, cancelled);
		eventPublisher.publishCancelled(cancelled.getId());
	}

	private <T> PagedResponse<T> createPagedResponse(List<T> allElements, int page, int size) {
		int totalElements = allElements.size();
		int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
		int from = page * size;
		int to = Math.min(from + size, totalElements);

		List<T> content = (from >= totalElements || from < 0) ? List.of() : allElements.subList(from, to);
		return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
	}
}
