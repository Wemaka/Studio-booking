package org.example.graphql.types.paging;

import org.example.dto.response.BookingResponse;

import java.util.List;

public record BookingConnectionGql(
		List<BookingResponse> content,
		PageInfoGql pageInfo,
		int totalElements
) {}