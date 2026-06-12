package org.example.publisher;

import org.example.events.EventEnvelope;
import org.example.events.RoutingKeys;
import org.example.events.StudioEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикация событий обогащения (studio.enriched) в RabbitMQ.
 *
 * Аналогичен StudioEventPublisher в studio-rest, но публикует другой тип события.
 * Паттерн fire-and-forget: если RabbitMQ недоступен, ошибка логируется,
 * но gRPC-вызов уже выполнен — результат не теряется полностью.
 */
@Component
public class EnrichmentEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(EnrichmentEventPublisher.class);
	private static final String SOURCE = "grpc-enrichment-client";

	private final RabbitTemplate rabbitTemplate;

	public EnrichmentEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	/**
	 * Публикует событие studio.enriched с результатами gRPC-аналитики.
	 */
	public void publishEnriched(StudioEvent.Enriched enrichedEvent) {
		try {
			EventEnvelope<StudioEvent> envelope = EventEnvelope.wrap(
					enrichedEvent, SOURCE, RoutingKeys.STUDIO_ENRICHED);

			rabbitTemplate.convertAndSend(
					RoutingKeys.EXCHANGE,
					RoutingKeys.STUDIO_ENRICHED,
					envelope);

			log.info("Событие отправлено: {} [studioId={}, eventId={}]",
					RoutingKeys.STUDIO_ENRICHED,
					enrichedEvent.studioId(),
					envelope.metadata().eventId());

		} catch (Exception e) {
			log.error("Не удалось отправить событие {}: {}",
					RoutingKeys.STUDIO_ENRICHED, e.getMessage());
		}
	}
}

