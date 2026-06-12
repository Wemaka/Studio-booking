package org.example.listener;

import org.example.events.EventMetadata;
import org.example.events.StudioEvent;
import org.example.grpc.AnalyzeStudioRequest;
import org.example.grpc.StudioAnalysisResponse;
import org.example.grpc.StudioAnalyticsGrpc;
import org.example.publisher.EnrichmentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * Слушатель событий studio.created из RabbitMQ.
 *
 * Десериализация — ручная (как в audit-service), потому что EventEnvelope<T>
 * является generic-типом, и Jackson не может определить конкретный подтип T.
 */
@Component
public class StudioCreatedListener {

	private static final Logger log = LoggerFactory.getLogger(StudioCreatedListener.class);

	private final StudioAnalyticsGrpc.StudioAnalyticsBlockingStub analyticsStub;
	private final EnrichmentEventPublisher enrichmentPublisher;
	private final JsonMapper jsonMapper;

	public StudioCreatedListener(StudioAnalyticsGrpc.StudioAnalyticsBlockingStub analyticsStub,
	                           EnrichmentEventPublisher enrichmentPublisher,
	                           JsonMapper jsonMapper) {
		this.analyticsStub = analyticsStub;
		this.enrichmentPublisher = enrichmentPublisher;
		this.jsonMapper = jsonMapper;
	}

	/**
	 * Обрабатывает событие studio.created:
	 * 1. Десериализует событие из JSON
	 * 2. Формирует gRPC-запрос
	 * 3. Вызывает gRPC-сервер (синхронно)
	 * 4. Публикует результат как событие studio.enriched
	 */
	@RabbitListener(queues = "q.enrichment.studio-created", messageConverter = "")
	public void handleStudioCreated(Message message) {
		try {
			// 1. Парсим JSON-конверт
			byte[] body = message.getBody();
			JsonNode root = jsonMapper.readTree(body);

			JsonNode metaNode = root.get("metadata");
			EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

			JsonNode payloadNode = root.get("payload");
			StudioEvent.Created studioCreated = jsonMapper.treeToValue(payloadNode, StudioEvent.Created.class);

			log.info("Получено событие studio.created: studioId={}, «{}» [eventId={}]",
					studioCreated.studioId(), studioCreated.name(), metadata.eventId());

			// 2. Формируем gRPC-запрос
			// Период: последние 30 дней (для новой студии история пустая, но запрос всё равно выполним)
			LocalDate endDate = LocalDate.now();
			LocalDate startDate = endDate.minus(30, ChronoUnit.DAYS);
			Timestamp startTs = toTimestamp(startDate.atStartOfDay(ZoneOffset.UTC).toInstant());
			Timestamp endTs = toTimestamp(endDate.atStartOfDay(ZoneOffset.UTC).toInstant());

			AnalyzeStudioRequest grpcRequest = AnalyzeStudioRequest.newBuilder()
					.setStudioId(studioCreated.studioId())
					.setName(studioCreated.name())
					.setPricePerHour(studioCreated.pricePerHour())
					.setTotalBooking(0)
					.setTotalDurationSum(0L)
					.setStartDate(startTs)
					.setEndDate(endTs)
					.build();

			// 3. Вызываем gRPC-сервер (синхронно)
			log.info("Вызов gRPC: StudioAnalytics.AnalyzeStudio(studioId={})", studioCreated.studioId());
			StudioAnalysisResponse grpcResponse = analyticsStub.analyzeStudio(grpcRequest);

			log.info("gRPC ответ: studioId={}, заполняемость={}%, рек.балл={}",
					grpcResponse.getStudioId(), grpcResponse.getOccupancyRate(),
					grpcResponse.getRecommendationScore());

			// 4. Публикуем событие studio.enriched
			StudioEvent.Enriched enrichedEvent = new StudioEvent.Enriched(
					grpcResponse.getStudioId(),
					grpcResponse.getName(),
					grpcResponse.getOccupancyRate(),
					grpcResponse.getTotalRevenue(),
					grpcResponse.getAvgBookingDuration(),
					grpcResponse.getRecommendationScore()
			);

			enrichmentPublisher.publishEnriched(enrichedEvent);

			log.info("Студия обогащена: studioId={} и studio.enriched отправлено", studioCreated.studioId());

		} catch (io.grpc.StatusRuntimeException e) {
			log.error("gRPC ошибка при обогащении студии: {} ({})",
					e.getStatus().getDescription(), e.getStatus().getCode());
			throw new RuntimeException("gRPC-вызов завершился ошибкой", e);
		} catch (Exception e) {
			log.error("Ошибка обработки события studio.created: {}", e.getMessage(), e);
			throw new RuntimeException("Не удалось обработать событие studio.created", e);
		}
	}

	private Timestamp toTimestamp(Instant instant) {
		return Timestamp.newBuilder()
				.setSeconds(instant.getEpochSecond())
				.setNanos(instant.getNano())
				.build();
	}
}

