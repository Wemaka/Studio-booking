package org.example.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "studios", itemRelation = "studio")
@Schema(description = "Информация о студии записи")
public class StudioResponse extends RepresentationModel<StudioResponse> {
	@Schema(description = "ID студии", example = "1")
	private final Long id;

	@Schema(description = "Название студии", example = "Studio A - Vocal Room")
	private final String name;

	@Schema(description = "Локация студии", example = "Москва, ул. Пушкина, 10")
	private final String location;

	@Schema(description = "Начало времени работы")
	private final String workingHoursStart;

	@Schema(description = "Конец времени работы")
	private final String workingHoursEnd;

	@Schema(description = "Доступна ли для записи")
	private final Boolean isActive;

	@Schema(description = "Цена студии за час бронирования", example = "1")
	private final Integer pricePerHour;
}