package org.example.graphql.types.paging;

import org.example.dto.response.StudioResponse;

import java.util.List;

public record StudioConnectionGql(
		List<StudioResponse> content,
		PageInfoGql pageInfo,
		int totalElements
) {}
