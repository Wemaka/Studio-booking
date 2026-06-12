package org.example.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import org.example.dto.enums.BookingStatus;
import org.example.dto.request.BookingRequest;
import org.example.dto.request.UpdateBookingRequest;
import org.example.dto.response.BookingResponse;
import org.example.dto.response.PagedResponse;
import org.example.graphql.types.filter.BookingFilterGql;
import org.example.graphql.types.input.CreateBookingInputGql;
import org.example.graphql.types.input.UpdateBookingInputGql;
import org.example.graphql.types.paging.BookingConnectionGql;
import org.example.graphql.types.paging.PageInfoGql;
import org.example.service.BookingService;

import java.time.OffsetDateTime;

@DgsComponent
public class BookingDataFetcher {

	private final BookingService bookingService;

	public BookingDataFetcher(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@DgsQuery
	public BookingResponse bookingById(@InputArgument String id) {
		return bookingService.getBookingById(Long.parseLong(id));
	}

	@DgsQuery
	public BookingConnectionGql bookings(
			@InputArgument BookingFilterGql filter,
			@InputArgument Integer page,
			@InputArgument Integer size) {

		int pageNum = page != null ? page : 0;
		int pageSize = size != null ? size : 20;

		Long clientId = null;
		Long studioId = null;
		OffsetDateTime startTime = null;
		Integer durationMinutes = null;
		BookingStatus status = null;

		if (filter != null) {
			clientId = filter.clientId();
			studioId = filter.studioId();
			startTime = filter.startTime();
			durationMinutes = filter.durationMinutes();
			status = filter.status();
		}

		PagedResponse<BookingResponse> paged = bookingService.getAllBookings(
				clientId, studioId, startTime, durationMinutes, status, pageNum, pageSize);

		return new BookingConnectionGql(
				paged.content(),
				new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
				(int) paged.totalElements());
	}

	@DgsMutation
	public BookingResponse createBooking(@InputArgument CreateBookingInputGql input) {
		BookingRequest bookingRequest = new BookingRequest(
				input.clientId(),
				input.studioId(),
				input.startTime(),
				input.durationMinutes(),
				input.clientNotes()
		);
		return bookingService.createBooking(bookingRequest);
	}

	@DgsMutation
	public BookingResponse updateBooking(@InputArgument String id, @InputArgument UpdateBookingInputGql input) {
		UpdateBookingRequest bookingRequest = new UpdateBookingRequest(
				input.startTime(),
				input.durationMinutes(),
				input.status(),
				input.clientNotes()
		);
		return bookingService.updateBooking(Long.parseLong(id), bookingRequest);
	}

	@DgsMutation
	public void cancelBooking(@InputArgument String id) {
		bookingService.cancelBooking(Long.parseLong(id));
	}
}
