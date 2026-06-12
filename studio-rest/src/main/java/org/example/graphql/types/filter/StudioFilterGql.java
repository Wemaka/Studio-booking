package org.example.graphql.types.filter;

public record StudioFilterGql(
		String name,
		String location,
		Integer pricePerHour,
		Boolean isActive
) {
}
