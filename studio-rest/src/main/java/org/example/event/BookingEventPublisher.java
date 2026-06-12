package org.example.event;

import org.example.dto.response.BookingResponse;
import org.example.events.BookingEvent;
import org.example.events.EventEnvelope;
import org.example.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Публикация доменных событий бронирования студий в RabbitMQ.
 * * Паттерн: вызов происходит после успешного коммита бизнес-операции.
 * Если брокер временно недоступен — ошибка перехватывается, чтобы не ломать
 * работу клиента, но логируется для последующего разбора.
 */
@Component
public class BookingEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(BookingEventPublisher.class);

	// Имя источника события для метаданных конверта
	private static final String SOURCE = "studio-rest";

	private final RabbitTemplate rabbitTemplate;

	public BookingEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	/**
	 * Публикует событие «Бронирование создано».
	 * (Вместо параметров можно передавать ваш объект BookingResponse/BookingEntity)
	 */
	public void publishCreated(BookingResponse booking) {
		var event = new BookingEvent.Created(
				booking.getId(),
				booking.getClientId(),
				booking.getStudio().getId(),
				booking.getStartTime(),
				booking.getDurationMinutes(),
				booking.getClientNotes()
		);

		send(RoutingKeys.BOOKING_CREATED, event);
	}

	public void publishUpdated(BookingResponse booking) {
		var event = new BookingEvent.Updated(
				booking.getId(),
				booking.getStudio().getId(),
				booking.getStartTime(),
				booking.getDurationMinutes(),
				booking.getStatus().name(),
				booking.getClientNotes()
		);

		send(RoutingKeys.BOOKING_UPDATED, event);
	}

	/**
	 * Публикует событие «Бронирование отменено».
	 */
	public void publishCancelled(Long bookingId) {
		var event = new BookingEvent.Cancelled(
				bookingId
		);

		send(RoutingKeys.BOOKING_CANCELLED, event);
	}

	/**
	 * Внутренний метод отправки события, заворачивающий его в EventEnvelope
	 */
	private void send(String routingKey, BookingEvent event) {
		try {
			EventEnvelope<BookingEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);

			// Отправляем в exchange с указанным ключом маршрутизации
			rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);

			log.info("Доменное событие отправлено в RabbitMQ: {} [eventId={}]",
					routingKey, envelope.metadata().eventId());
		} catch (Exception e) {
			log.error("Критическая ошибка при отправке события {} в RabbitMQ: {}",
					routingKey, e.getMessage(), e);
		}
	}
}
