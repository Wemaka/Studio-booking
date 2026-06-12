package org.example.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import org.example.dto.response.AvailableSlotResponse;
import org.example.dto.response.PagedResponse;
import org.example.graphql.types.filter.AvailableSlotFilterGql;
import org.example.graphql.types.paging.AvailableSlotConnectionGql;
import org.example.graphql.types.paging.PageInfoGql;
import org.example.service.StudioService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@DgsComponent
public class AvailableSlotsDataFetcher {

	private final StudioService studioService;

	public AvailableSlotsDataFetcher(StudioService studioService) {
		this.studioService = studioService;
	}

	@DgsQuery
	public AvailableSlotConnectionGql availableSlots(
			@InputArgument AvailableSlotFilterGql filter,
			@InputArgument Integer page,
			@InputArgument Integer size) {

		try {
			int pageNum = page != null ? page : 0;
			int pageSize = size != null ? size : 10;

			Long studioId = null;
			LocalDate date = null;
			LocalTime time = null;
			Integer durationMinutes = null;

			if (filter != null) {
				studioId = filter.studioId();
				date = filter.date();
				time = filter.time();
				durationMinutes = filter.durationMinutes();
			}

			PagedResponse<AvailableSlotResponse> paged = studioService.getAllAvailableSlots(
					studioId, date, time, durationMinutes, pageNum, pageSize);

			return new AvailableSlotConnectionGql(
					paged.content(),
					new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
					(int) paged.totalElements());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error in availableSlots: " + e.getMessage(), e);
		}
	}
}
