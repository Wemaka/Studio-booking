package org.example.graphql.types.paging;

import org.example.dto.response.AvailableSlotResponse;

import java.util.List;

public record AvailableSlotConnectionGql(
		List<AvailableSlotResponse> content,
		PageInfoGql pageInfo,
		int totalElements
) {
}
