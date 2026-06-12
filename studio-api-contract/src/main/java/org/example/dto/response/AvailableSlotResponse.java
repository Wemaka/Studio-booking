package org.example.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.OffsetDateTime;

@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "availableslots", itemRelation = "availableslot")
@Schema(description = "Доступный слот с рассчитанной ценой")
public class AvailableSlotResponse extends RepresentationModel<AvailableSlotResponse> {
	@Schema(description = "Студия")
	private final StudioResponse studio;

	@Schema(description = "Начало слота")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
	private final OffsetDateTime startTime;

	@Schema(description = "Конец слота")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
	private final OffsetDateTime endTime;

	@Schema(description = "Длительность в минутах", example = "120")
	private final Integer durationMinutes;

	@Schema(description = "Общая стоимость аренды за слот", example = "5000")
	private final Integer totalPrice;
}
