package org.example.controllers;

import jakarta.validation.Valid;
import org.example.assemblers.BookingModelAssembler;
import org.example.dto.enums.BookingStatus;
import org.example.dto.request.BookingRequest;
import org.example.dto.request.PatchBookingRequest;
import org.example.dto.request.UpdateBookingRequest;
import org.example.dto.response.BookingResponse;
import org.example.dto.response.PagedResponse;
import org.example.endpoints.BookingApi;
import org.example.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
public class BookingController implements BookingApi {

	private final BookingService bookingService;
	private final BookingModelAssembler assembler;
	private final PagedResourcesAssembler<BookingResponse> pagedResourcesAssembler;

	public BookingController(BookingService bookingService, BookingModelAssembler assembler, PagedResourcesAssembler<BookingResponse> pagedResourcesAssembler) {
		this.bookingService = bookingService;
		this.assembler = assembler;
		this.pagedResourcesAssembler = pagedResourcesAssembler;
	}

	@Override
	public EntityModel<BookingResponse> getBookingById(Long id) {
		BookingResponse booking = bookingService.getBookingById(id);
		return assembler.toModel(booking);
	}

	@Override
	public PagedModel<EntityModel<BookingResponse>> getAllBookings(
			Long clientId,
			Long studioId,
			OffsetDateTime startTime,
			Integer durationMinutes,
			BookingStatus status,
			int page,
			int size
	) {
		PagedResponse<BookingResponse> paged = bookingService.getAllBookings(
				clientId, studioId, startTime, durationMinutes, status, page, size);

		Page<BookingResponse> springPage = new PageImpl<>(
				paged.content(),
				PageRequest.of(paged.pageNumber(), paged.pageSize()),
				paged.totalElements()
		);

		return pagedResourcesAssembler.toModel(springPage, assembler);
	}

	@Override
	public ResponseEntity<EntityModel<BookingResponse>> createBooking(BookingRequest request) {
		BookingResponse created = bookingService.createBooking(request);
		EntityModel<BookingResponse> model = assembler.toModel(created);
		return ResponseEntity
				.created(model.getRequiredLink("self").toUri())
				.body(model);
	}

	@Override
	public EntityModel<BookingResponse> updateBooking(Long id, UpdateBookingRequest request) {
		BookingResponse updated = bookingService.updateBooking(id, request);
		return assembler.toModel(updated);
	}

	@Override
	public EntityModel<BookingResponse> patchBooking(Long id, PatchBookingRequest request) {
		BookingResponse patched = bookingService.patchBooking(id, request);
		return assembler.toModel(patched);
	}

	@Override
	public void cancelBooking(@PathVariable Long id) {
		bookingService.cancelBooking(id);
	}
}
