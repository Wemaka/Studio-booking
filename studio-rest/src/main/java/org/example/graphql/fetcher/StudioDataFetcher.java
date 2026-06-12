package org.example.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import org.example.dto.request.StudioRequest;
import org.example.dto.request.UpdateStudioRequest;
import org.example.dto.response.PagedResponse;
import org.example.dto.response.StudioResponse;
import org.example.graphql.types.filter.StudioFilterGql;
import org.example.graphql.types.input.CreateStudioInputGql;
import org.example.graphql.types.paging.PageInfoGql;
import org.example.graphql.types.paging.StudioConnectionGql;
import org.example.service.StudioService;

@DgsComponent
public class StudioDataFetcher {

	private final StudioService studioService;

	public StudioDataFetcher(StudioService studioService) {
		this.studioService = studioService;
	}

	@DgsQuery
	public StudioResponse studioById(@InputArgument String id) {
		return studioService.getStudioById(Long.parseLong(id));
	}

	@DgsQuery
	public StudioConnectionGql studios(
			@InputArgument StudioFilterGql filter,
			@InputArgument Integer page,
			@InputArgument Integer size) {

		int pageNum = page != null ? page : 0;
		int pageSize = size != null ? size : 20;

		String name = null;
		String location = null;
		Integer pricePerHour = null;
		Boolean isActive = null;

		if (filter != null) {
			name = filter.name();
			location = filter.location();
			pricePerHour = filter.pricePerHour();
			isActive = filter.isActive();
		}

		PagedResponse<StudioResponse> paged = studioService.getAllStudios(
				pricePerHour, name, location, isActive, pageNum, pageSize);

		return new StudioConnectionGql(
				paged.content(),
				new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
				(int) paged.totalElements());
	}

	@DgsMutation
	public StudioResponse createStudio(@InputArgument CreateStudioInputGql input) {
		StudioRequest request = new StudioRequest(
				input.name(),
				input.location(),
				input.workingHoursStart(),
				input.workingHoursEnd(),
				input.isActive() != null ? input.isActive() : true,
				input.pricePerHour()
		);
		return studioService.createStudio(request);
	}

	@DgsMutation
	public StudioResponse updateStudio(@InputArgument String id, @InputArgument UpdateStudioRequest input) {
		UpdateStudioRequest request = new UpdateStudioRequest(
				input.name(),
				input.location(),
				input.workingHoursStart(),
				input.workingHoursEnd(),
				input.isActive(),
				input.pricePerHour()
		);
		return studioService.updateStudio(Long.parseLong(id), request);
	}
}
