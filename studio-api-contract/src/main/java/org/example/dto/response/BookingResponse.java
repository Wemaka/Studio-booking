package org.example.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.example.dto.enums.BookingStatus;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "bookings", itemRelation = "booking")
@Schema(description = "Данные брони студии (ответ)")
public class BookingResponse extends RepresentationModel<BookingResponse> {
	@Schema(description = "ID брони", example = "100")
	private final Long id;

	@Schema(description = "ID клиента", example = "client-uuid-123")
	private final String clientId;

	@Schema(description = "Студия")
	private final StudioResponse studio;

	@Schema(description = "Время начала сессии")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
	private final OffsetDateTime startTime;

	@Schema(description = "Длительность в минутах", example = "120")
	private final Integer durationMinutes;

	@Schema(description = "Статус брони", example = "PENDING")
	private final BookingStatus status;

	@Schema(description = "Комментарий клиента")
	private final String clientNotes;
}