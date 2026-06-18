package org.example.service;

import org.example.dto.enums.BookingStatus;
import org.example.dto.request.PatchStudioRequest;
import org.example.dto.request.StudioRequest;
import org.example.dto.request.UpdateStudioRequest;
import org.example.dto.response.*;
import org.example.event.StudioEventPublisher;
import org.example.exception.NotFoundException;
import org.example.storage.InMemoryStorage;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StudioService {
	private final InMemoryStorage storage;
	private final StudioEventPublisher eventPublisher;

	public StudioService(InMemoryStorage storage, StudioEventPublisher eventPublisher) {
		this.storage = storage;
		this.eventPublisher = eventPublisher;
	}

	public StudioResponse getStudioById(Long id) {
		return Optional.ofNullable(storage.studios.get(id))
				.orElseThrow(() -> new NotFoundException("Studio", id.toString()));
	}

	public PagedResponse<StudioResponse> getAllStudios(
			Integer pricePerHour, String name, String location, Boolean isActive,
			int page, int size
	) {
		Stream<StudioResponse> stream = storage.studios.values().stream();

		if (pricePerHour != null) {
			stream = stream.filter(s -> s.getPricePerHour().equals(pricePerHour));
		}
		if (name != null && !name.isBlank()) {
			stream = stream.filter(s -> s.getName().toLowerCase().contains(name.toLowerCase()));
		}
		if (location != null && !location.isBlank()) {
			stream = stream.filter(s -> s.getLocation().toLowerCase().contains(location.toLowerCase()));
		}
		if (isActive != null) {
			stream = stream.filter(s -> s.getIsActive().equals(isActive));
		}

		List<StudioResponse> filtered = stream.toList();
		return createPagedResponse(filtered, page, size);
	}

	public PagedResponse<AvailableSlotResponse> getAllAvailableSlots(
			Long studioId, LocalDate date, LocalTime time, Integer durationMinutes,
			int page, int size
	) {
		int effectiveDuration = (durationMinutes != null && durationMinutes > 0) ? durationMinutes : 60;
		LocalDate targetDate = (date != null) ? date : LocalDate.now();

		Stream<StudioResponse> studioStream = storage.studios.values().stream()
				.filter(StudioResponse::getIsActive);

		if (studioId != null) {
			studioStream = studioStream.filter(s -> studioId.equals(s.getId()));
		}

		List<AvailableSlotResponse> allSlots = studioStream.flatMap(studio -> {
			LocalTime startParsed = LocalTime.parse(studio.getWorkingHoursStart());
			LocalTime endParsed = LocalTime.parse(studio.getWorkingHoursEnd());

			LocalDateTime workStart = LocalDateTime.of(targetDate, startParsed);
			LocalDateTime workEnd = LocalDateTime.of(targetDate, endParsed);

			if (workEnd.isBefore(workStart) || workEnd.isEqual(workStart)) {
				workEnd = workEnd.plusDays(1);
			}

			final LocalDateTime finalWorkStart = workStart;
			final LocalDateTime finalWorkEnd = workEnd;

			List<BookingResponse> bookings = storage.bookings.values().stream()
					.filter(b -> b.getStudio().getId().equals(studio.getId()))
					.filter(b -> b.getStatus() != BookingStatus.CANCELLED)
					.filter(b -> {
						LocalDateTime bStart = b.getStartTime().toLocalDateTime();
						LocalDateTime bEnd = bStart.plusMinutes(b.getDurationMinutes());
						return !(bEnd.isBefore(finalWorkStart) || bStart.isAfter(finalWorkEnd));
					})
					.toList();

			List<Interval> freeIntervals = calculateFreeIntervals(workStart, workEnd, bookings);
			List<AvailableSlotResponse> studioSlots = new ArrayList<>();

			for (Interval free : freeIntervals) {
				if (time != null) {
					// Конкретное время — проверяем что оно попадает в свободный интервал
					if (time.isBefore(free.start().toLocalTime())
							|| !time.isBefore(free.end().toLocalTime())) {
						continue;
					}
					LocalDateTime slotStart = LocalDateTime.of(targetDate, time);
					long available = Duration.between(slotStart, free.end()).toMinutes();

					if (durationMinutes != null) {
						if (available < durationMinutes) continue;
						studioSlots.add(buildSlot(studio, slotStart, durationMinutes, targetDate));
					} else {
						if (available < effectiveDuration) continue;
						studioSlots.add(buildSlot(studio, slotStart, available, targetDate));
					}

				} else if (durationMinutes != null) {
					// Нарезаем свободный интервал на равные слоты по durationMinutes
					LocalDateTime slotStart = free.start();
					while (!slotStart.plusMinutes(durationMinutes).isAfter(free.end())) {
						studioSlots.add(buildSlot(studio, slotStart, durationMinutes, targetDate));
						slotStart = slotStart.plusMinutes(durationMinutes);
					}

				} else {
					// Без durationMinutes — один слот на весь свободный интервал
					long available = free.durationMinutes();
					if (available < effectiveDuration) continue;
					studioSlots.add(buildSlot(studio, free.start(), available, targetDate));
				}
			}
			return studioSlots.stream();
		}).collect(Collectors.toList());

		return createPagedResponse(allSlots, page, size);
	}

	public StudioResponse createStudio(StudioRequest request) {
		long id = storage.studioSequence.incrementAndGet();
		StudioResponse studio = StudioResponse.builder()
				.id(id)
				.name(request.name())
				.location(request.location())
				.workingHoursStart(request.workingHoursStart())
				.workingHoursEnd(request.workingHoursEnd())
				.isActive(request.isActive())
				.pricePerHour(request.pricePerHour())
				.build();

		storage.studios.put(id, studio);
		eventPublisher.publishCreated(studio);
		return studio;
	}

	public StudioResponse updateStudio(Long id, UpdateStudioRequest request) {
		StudioResponse updated = StudioResponse.builder()
				.id(id)
				.name(request.name())
				.location(request.location())
				.workingHoursStart(request.workingHoursStart())
				.workingHoursEnd(request.workingHoursEnd())
				.isActive(request.isActive())
				.pricePerHour(request.pricePerHour())
				.build();

		storage.studios.put(id, updated);
		eventPublisher.publishUpdated(updated);
		return updated;
	}

	public StudioResponse patchStudio(Long id, PatchStudioRequest request) {
		StudioResponse existing = getStudioById(id);

		StudioResponse updated = StudioResponse.builder()
				.id(id)
				.name(request.name() != null ? request.name() : existing.getName())
				.location(request.location() != null ? request.location() : existing.getLocation())
				.workingHoursStart(request.workingHoursStart() != null ? request.workingHoursStart() : existing.getWorkingHoursStart())
				.workingHoursEnd(request.workingHoursEnd() != null ? request.workingHoursEnd() : existing.getWorkingHoursEnd())
				.isActive(request.isActive() != null ? request.isActive() : existing.getIsActive())
				.pricePerHour(request.pricePerHour() != null ? request.pricePerHour() : existing.getPricePerHour())
				.build();

		storage.studios.put(id, updated);
		eventPublisher.publishUpdated(updated);
		return updated;
	}

	private <T> PagedResponse<T> createPagedResponse(List<T> allElements, int page, int size) {
		int totalElements = allElements.size();
		int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
		int from = page * size;
		int to = Math.min(from + size, totalElements);

		List<T> content = (from >= totalElements || from < 0) ? List.of() : allElements.subList(from, to);
		return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
	}

	private record Interval(LocalDateTime start, LocalDateTime end) {
		public long durationMinutes() {
			return Duration.between(start, end).toMinutes();
		}
	}

	private List<Interval> calculateFreeIntervals(LocalDateTime workStart, LocalDateTime workEnd, List<BookingResponse> bookings) {
		// Преобразуем бронирования в интервалы (LocalDateTime start, LocalDateTime end)
		List<Interval> busyIntervals = bookings.stream()
				.map(b -> {
					LocalDateTime start = b.getStartTime().toLocalDateTime();
					LocalDateTime end = start.plusMinutes(b.getDurationMinutes());
					return new Interval(start, end);
				})
				.sorted(Comparator.comparing(Interval::start))
				.toList();

		// Объединяем пересекающиеся бронирования
		List<Interval> mergedBusy = new ArrayList<>();
		for (Interval busy : busyIntervals) {
			if (mergedBusy.isEmpty() || mergedBusy.get(mergedBusy.size() - 1).end().isBefore(busy.start())) {
				mergedBusy.add(busy);
			} else {
				Interval last = mergedBusy.get(mergedBusy.size() - 1);
				LocalDateTime newEnd = last.end().isAfter(busy.end()) ? last.end() : busy.end();
				mergedBusy.set(mergedBusy.size() - 1, new Interval(last.start(), newEnd));
			}
		}

		// Вырезаем свободные интервалы из рабочего дня
		List<Interval> free = new ArrayList<>();
		LocalDateTime curStart = workStart;
		for (Interval busy : mergedBusy) {
			if (curStart.isBefore(busy.start())) {
				free.add(new Interval(curStart, busy.start()));
			}
			curStart = busy.end();
		}
		if (curStart.isBefore(workEnd)) {
			free.add(new Interval(curStart, workEnd));
		}
		return free;
	}

	private AvailableSlotResponse buildSlot(StudioResponse studio,
	                                        LocalDateTime slotStart,
	                                        long durationMinutes,
	                                        LocalDate targetDate) {
		ZoneOffset offset = OffsetDateTime.now().getOffset();
		return AvailableSlotResponse.builder()
				.studio(studio)
				.startTime(slotStart.atOffset(offset))
				.endTime(slotStart.plusMinutes(durationMinutes).atOffset(offset))
				.durationMinutes((int) durationMinutes)
				.totalPrice(studio.getPricePerHour() * (int) durationMinutes / 60)
				.build();
	}
}
