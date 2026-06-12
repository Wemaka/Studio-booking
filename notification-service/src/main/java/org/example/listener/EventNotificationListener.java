package org.example.listener;

import org.example.events.BookingEvent;
import org.example.events.EventMetadata;
import org.example.events.StudioEvent;
import org.example.websocket.NotificationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Слушатель всех доменных событий из RabbitMQ.
 *
 * Получает события из очереди q.notifications.all (binding "#"),
 * формирует человекочитаемое JSON-уведомление и рассылает
 * всем подключённым WebSocket-клиентам через NotificationWebSocketHandler.
 *
 * Дедупликация — по eventId (на случай повторной доставки RabbitMQ).
 */
@Component
public class EventNotificationListener {

	private static final Logger log = LoggerFactory.getLogger(EventNotificationListener.class);

	private final NotificationWebSocketHandler webSocketHandler;
	private final JsonMapper jsonMapper;

	/** Набор обработанных eventId для дедупликации. */
	private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

	public EventNotificationListener(NotificationWebSocketHandler webSocketHandler,
	                                 JsonMapper jsonMapper) {
		this.webSocketHandler = webSocketHandler;
		this.jsonMapper = jsonMapper;
	}

	@RabbitListener(queues = "q.notifications.all", messageConverter = "")
	public void handleEvent(Message message) {
		try {
			byte[] body = message.getBody();
			JsonNode root = jsonMapper.readTree(body);

			// Парсим метаданные
			JsonNode metaNode = root.get("metadata");
			EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

			// Дедупликация по eventId
			if (!processedEventIds.add(metadata.eventId())) {
				log.warn("Дубликат уведомления пропущен: eventId={}", metadata.eventId());
				return;
			}

			// Формируем уведомление
			JsonNode payloadNode = root.get("payload");
			String title = buildTitle(metadata.eventType());
			String description = buildDescription(metadata.eventType(), payloadNode);
			String icon = resolveIcon(metadata.eventType());
			String level = resolveLevel(metadata.eventType());

			// JSON для WebSocket-клиента
			String notificationJson = jsonMapper.writeValueAsString(
					new NotificationPayload(
							"NOTIFICATION",
							metadata.eventId(),
							metadata.eventType(),
							title,
							description,
							icon,
							level,
							metadata.sourceService(),
							metadata.timestamp().toString(),
							Instant.now().toString()
					)
			);

			// Broadcast в WebSocket
			webSocketHandler.broadcast(notificationJson);

			log.info("[NOTIFY] {} | {} (клиентов: {})",
					metadata.eventType(), description, webSocketHandler.getActiveConnectionCount());

		} catch (Exception e) {
			log.error("Ошибка обработки события для уведомлений: {}", e.getMessage(), e);
			throw new RuntimeException("Не удалось обработать событие", e);
		}
	}

	// Формирование заголовка уведомления

	private String buildTitle(String eventType) {
		return switch (eventType) {
			case "book.created"   -> "Новая книга";
			case "book.updated"   -> "Книга обновлена";
			case "book.deleted"   -> "Книга удалена";
			case "book.enriched"  -> "Аналитика книги";
			case "author.created" -> "Новый автор";
			case "author.deleted" -> "Автор удалён";
			default               -> "Событие: " + eventType;
		};
	}

	// Формирование описания

	private String buildDescription(String eventType, JsonNode payloadNode) {
		try {
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
		} catch (Exception e) {
			return "Событие " + eventType + " (ошибка парсинга)";
		}
	}

	// Иконка по типу события

	private String resolveIcon(String eventType) {
		return switch (eventType) {
			case "studio.created"   -> "studio-plus";
			case "studio.updated"   -> "studio-edit";
			case "studio.enriched"  -> "analytics";
			case "booking.created" -> "booking-plus";
			case "booking.updated" -> "booking-edit";
			case "booking.cancelled" -> "booking-cancel";
			default               -> "bell";
		};
	}

	// Уровень уведомления

	private String resolveLevel(String eventType) {
		return switch (eventType) {
			case "studio.updated", "booking.updated", "booking.cancelled" -> "warning";
			case "studio.enriched"                  -> "info";
			default                               -> "success";
		};
	}

	/**
	 * Payload уведомления для WebSocket.
	 */
	record NotificationPayload(
			String type,
			String eventId,
			String eventType,
			String title,
			String description,
			String icon,
			String level,
			String source,
			String eventTimestamp,
			String receivedAt
	) {}
}
