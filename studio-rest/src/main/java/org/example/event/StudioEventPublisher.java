package org.example.event;

import org.example.dto.response.StudioResponse;
import org.example.events.EventEnvelope;
import org.example.events.RoutingKeys;
import org.example.events.StudioEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class StudioEventPublisher {
	private static final Logger log = LoggerFactory.getLogger(StudioEventPublisher.class);

	// Имя источника события для метаданных конверта
	private static final String SOURCE = "studio-rest";

	private final RabbitTemplate rabbitTemplate;

	public StudioEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publishCreated(StudioResponse studio) {
		var event = new StudioEvent.Created(
				studio.getId(),
				studio.getName(),
				studio.getLocation(),
				studio.getWorkingHoursStart() != null ? LocalTime.parse(studio.getWorkingHoursStart()) : null,
				studio.getWorkingHoursEnd() != null ? LocalTime.parse(studio.getWorkingHoursEnd()) : null,
				studio.getPricePerHour()
		);

		send(RoutingKeys.STUDIO_CREATED, event);
	}

	public void publishUpdated(StudioResponse studio) {
		var event = new StudioEvent.Updated(
				studio.getId(),
				studio.getName(),
				studio.getLocation(),
				studio.getWorkingHoursStart() != null ? LocalTime.parse(studio.getWorkingHoursStart()) : null,
				studio.getWorkingHoursEnd() != null ? LocalTime.parse(studio.getWorkingHoursEnd()) : null,
				studio.getIsActive(),
				studio.getPricePerHour()
		);

		send(RoutingKeys.STUDIO_UPDATED, event);
	}

	private void send(String routingKey, StudioEvent event) {
		try {
			EventEnvelope<StudioEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);

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
