package org.example.graphql.types.input;

public record CreateStudioInputGql(
		String name,
		String location,
		String workingHoursStart,
		String workingHoursEnd,
		Boolean isActive,
		Integer pricePerHour
) {
}
