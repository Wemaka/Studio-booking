package org.example.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import org.example.dto.response.BookingResponse;
import org.example.dto.response.PagedResponse;
import org.example.dto.response.StudioResponse;
import org.example.graphql.types.paging.BookingConnectionGql;
import org.example.graphql.types.paging.PageInfoGql;
import org.example.service.BookingService;

@DgsComponent
public class StudioBookingsDataFetcher {

	private final BookingService bookingService;

	public StudioBookingsDataFetcher(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	/**
	 * Загружает бронирования для конкретной студии.
	 * Вызывается только когда клиент запросил поле bookings у студии.
	 */
	@DgsData(parentType = "Studio", field = "bookings")
	public BookingConnectionGql bookings(
			DgsDataFetchingEnvironment dfe,
			@InputArgument Integer page,
			@InputArgument Integer size) {

		StudioResponse studio = dfe.getSource();
		Long studioId = studio.getId();

		int pageNum = page != null ? page : 0;
		int pageSize = size != null ? size : 20;

		// Предполагается, что в BookingService есть метод findByStudioId
		PagedResponse<BookingResponse> paged = bookingService.getAllBookings(null, studioId, null
				, null, null, pageNum, pageSize);

		return new BookingConnectionGql(
				paged.content(),
				new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
				(int) paged.totalElements()
		);
	}
}
