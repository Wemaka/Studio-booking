package org.example.model;

import java.time.Instant;
import java.time.OffsetDateTime;

public record AuditEntry(

		// Порядковый номер записи в журнале (для сортировки и пагинации)
		long sequenceNumber,

		// Идентификатор события из EventMetadata — связь с исходным сообщением
		String eventId,

		// Тип события: "studio.created" и т.д.
		String eventType,

		// Сервис-источник события
		String source,

		// Момент создания события (на стороне publisher)
		Instant eventTimestamp,

		// Момент получения события audit-service (разница показывает задержку доставки)
		Instant receivedAt,

		// Человекочитаемое описание
		String description
) {}

