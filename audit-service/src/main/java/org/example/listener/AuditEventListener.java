package org.example.listener;

import org.example.events.BookingEvent;
import org.example.events.EventMetadata;
import org.example.events.StudioEvent;
import org.example.model.AuditEntry;
import org.example.storage.AuditStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Component
public class AuditEventListener {

	private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

	private final AuditStorage auditStorage;
	private final JsonMapper jsonMapper;

	public AuditEventListener(AuditStorage auditStorage, JsonMapper jsonMapper) {
		this.auditStorage = auditStorage;
		this.jsonMapper = jsonMapper;
	}

	/**
	 * Принимает все события из очереди q.audit.events.
	 *
	 * Десериализация выполняется в два этапа:
	 * 1. Парсим JSON в дерево узлов (JsonNode) — быстро и безопасно.
	 * 2. Извлекаем metadata и определяем тип payload по полю eventType.
	 * 3. Десериализуем payload в конкретный record по выявленному типу.
	 */
	@RabbitListener(queues = "q.audit.events", messageConverter = "")
	public void handleEvent(Message message) {
		try {
			byte[] body = message.getBody();
			JsonNode root = jsonMapper.readTree(body);

			// Извлекаем метаданные из JSON-конверта
			JsonNode metaNode = root.get("metadata");
			EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

			// Дедупликация — если событие уже обработано, пропускаем
			if (auditStorage.isDuplicate(metadata.eventId())) {
				log.warn("Дубликат события пропущен: eventId={}", metadata.eventId());
				return;
			}

			// Определяем тип события и формируем описание
			JsonNode payloadNode = root.get("payload");
			String description = buildDescription(metadata.eventType(), payloadNode);

			AuditEntry entry = auditStorage.save(new AuditEntry(
					0,
					metadata.eventId(),
					metadata.eventType(),
					metadata.sourceService(),
					metadata.timestamp(),
					Instant.now(),
					description
			));

			log.info("[AUDIT #{}] {} | {}", entry.sequenceNumber(), metadata.eventType(), description);

		} catch (Exception e) {
			log.error("Ошибка обработки события: {}", e.getMessage(), e);
			// Исключение пробросится, сообщение уйдёт в DLQ после исчерпания retries
			throw new RuntimeException("Не удалось обработать событие", e);
		}
	}

	/**
	 * Формирует человекочитаемое описание события для аудит-лога.
	 *
	 * Десериализует payload в конкретный тип на основе eventType,
	 * затем формирует описание через pattern matching по sealed interface.
	 */
	private String buildDescription(String eventType, JsonNode payloadNode) throws Exception {
		return switch (eventType) {
			case "studio.created" -> {
				StudioEvent.Created e = jsonMapper.treeToValue(payloadNode,
						StudioEvent.Created.class);
				yield String.format("Создана новая студия %s в локации [%s]. Стоимость: %d/ч",
						e.name(), e.location(), e.pricePerHour());
			}
			case "studio.updated" -> {
				StudioEvent.Updated e = jsonMapper.treeToValue(payloadNode,
						StudioEvent.Updated.class);
				yield String.format("Обновлены параметры студии #%d. Название: %s. " +
								"Местоположение: %s" +
								" Рабочее время: от %s до %s" +
								" Активна: %b Стоимость: %d/ч",
						e.studioId(), e.name(), e.location(),
						e.workingHoursStart(), e.workingHoursEnd(),
						e.isActive(), e.pricePerHour());
			}
			case "studio.enriched" -> {
				StudioEvent.Enriched e = jsonMapper.treeToValue(payloadNode, StudioEvent.Enriched.class);
				yield String.format("Студия обогащена аналитикой id=%d «%s»: занятость: %.1f%%, " +
								"выручка: %.2f, ср. время: %.1f мин, рекомендательный счет: %.2f",
						e.studioId(), e.name(), e.occupancyRate(), e.totalRevenue(),
						e.avgBookingDuration(), e.recommendationScore());
			}

			case "booking.created" -> {
				BookingEvent.Created e = jsonMapper.treeToValue(payloadNode,
						BookingEvent.Created.class);
				yield String.format("Создано бронирование №%d для клиента ID %s в студии ID %d. " +
								"Время начала: %s Продолжительность: %d",
						e.bookingId(), e.clientId(), e.studioId(), e.startTime(), e.durationMinutes());
			}
			case "booking.updated" -> {
				BookingEvent.Updated e = jsonMapper.treeToValue(payloadNode,
						BookingEvent.Updated.class);
				yield String.format("Изменено бронирование №%d в студии ID %d. " +
								"Время начала: %s Продолжительность: %d Статус: %s",
						e.bookingId(), e.studioId(), e.startTime(),
						e.durationMinutes(), e.status());
			}
			case "booking.cancelled" -> {
				BookingEvent.Cancelled e = jsonMapper.treeToValue(payloadNode,
						BookingEvent.Cancelled.class);
				yield String.format("Отменено бронирование №%d", e.bookingId());
			}

			default -> "Неизвестное событие: " + eventType;
		};
	}
}

