package org.example.graphql.types.input;

public record UpdateStudioInputGql(
		String name,
		String location,
		String workingHoursStart,
		String workingHoursEnd,
		Boolean isActive,
		Integer pricePerHour
) {
}
