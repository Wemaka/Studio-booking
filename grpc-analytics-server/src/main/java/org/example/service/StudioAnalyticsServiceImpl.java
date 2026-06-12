package org.example.service;

import io.grpc.stub.StreamObserver;
import org.example.grpc.AnalyzeStudioRequest;
import org.example.grpc.StudioAnalysisResponse;
import org.example.grpc.StudioAnalyticsGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Реализация gRPC-сервиса StudioAnalytics.
 *
 * Наследует сгенерированный базовый класс StudioAnalyticsImplBase —
 * аналог того, как REST-контроллер реализует интерфейс контракта:
 *
 *   REST:    AuthorController implements AuthorApi
 *   GraphQL: StudioDataFetcher с @DgsQuery
 *   gRPC:    StudioAnalyticsServiceImpl extends StudioAnalyticsGrpc.StudioAnalyticsImplBase
 *
 * Ключевые отличия от REST/GraphQL:
 * - Бинарный протокол (protobuf) вместо JSON — компактнее и быстрее
 * - Строго типизированный контракт (.proto) — несовместимость обнаруживается при компиляции
 * - HTTP/2 с мультиплексированием — несколько запросов в одном TCP-соединении
 * - Поддержка streaming (server, client, bidirectional) — здесь используем unary (простой запрос-ответ)
 */
public class StudioAnalyticsServiceImpl extends StudioAnalyticsGrpc.StudioAnalyticsImplBase {
	private static final Logger log = LoggerFactory.getLogger(StudioAnalyticsServiceImpl.class);

	/**
	 * Обрабатывает запрос на анализ студии.
	 *
	 * Паттерн gRPC: метод получает request и StreamObserver для ответа.
	 * StreamObserver — это callback-интерфейс:
	 *   - onNext(response) — отправить ответ (для unary RPC вызывается один раз)
	 *   - onCompleted()    — завершить RPC
	 *   - onError(t)       — сообщить об ошибке
	 *
	 * Для unary RPC (один запрос → один ответ) всегда:
	 *   responseObserver.onNext(response);
	 *   responseObserver.onCompleted();
	 */
	@Override
	public void analyzeStudio(AnalyzeStudioRequest request,
	                          StreamObserver<StudioAnalysisResponse> responseObserver) {

		log.info("gRPC запрос: аналитика для студии id={} «{}»",
				request.getStudioId(), request.getName());

		// Демонстрационный расчет метрик
		double occupancy = calculateOccupancy(request.getName(), request.getPricePerHour());
		double revenue = calculateRevenueScore(occupancy, request.getPricePerHour());
		double avgDuration = (request.getTotalBooking() == 0.0) ? 0.0 :
				request.getTotalDurationSum() / (double) request.getTotalBooking();
		double score = calculateRecommendationScore(request);

		StudioAnalysisResponse response = StudioAnalysisResponse.newBuilder()
				.setStudioId(request.getStudioId())
				.setOccupancyRate(occupancy)
				.setTotalRevenue(revenue)
				.setAvgBookingDuration(avgDuration)
				.setRecommendationScore(score)
				.build();

		log.info("gRPC ответ: студия id={}, заполняемость={}%, выручка={}",
				response.getStudioId(), occupancy, revenue);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	// ─── Демонстрационная бизнес-логика ──────────────────────────────
	/**
	 * Вычисляет процент заполняемости на основе
	 * популярности локации и ценового сегмента.
	 */
	private double calculateOccupancy(String name, int pricePerHour) {
		double base = 60.0; // Базовая загрузка 60%

		// Студии в центре или с низким прайсом популярнее
		if (name.toLowerCase().contains("центр") || name.toLowerCase().contains("center")) base += 15.0;
		if (pricePerHour < 1500) base += 20.0;

		// Студии премиум-класса загружены чуть меньше
		if (pricePerHour > 5000) base -= 10.0;

		return Math.min(base, 99.9);
	}

	/**
	 * Вычисляет "Индекс Эффективности" (Revenue Score).
	 * Демонстрирует, насколько эффективно студия конвертирует время в деньги.
	 */
	private double calculateRevenueScore(double occupancy, int pricePerHour) {
		// чем выше заполняемость и выше чек, тем круче показатель
		double score = (occupancy / 100.0) * (pricePerHour / 100.0);
		return Math.round(Math.min(score, 10.0) * 10.0) / 10.0;
	}

	/**
	 * Классификация типа студии по её названию/характеристикам.
	 */
	private String classifyStudioTier(int pricePerHour) {
		if (pricePerHour < 1000) return "BUDGET_ACCESSIBLE";
		if (pricePerHour < 3000) return "PRO_STANDARD";
		if (pricePerHour < 6000) return "PREMIUM_STUDIO";
		return "ULTRA_LUXURY_FACILITY";
	}

	/**
	 * 0–3 – студия с низкой активностью или очень дорогая; не рекомендуется.
	 * 4–6 – средние показатели, можно рассматривать.
	 * 7–10 – популярная, удобная и недорогая студия; максимальные рекомендации.
	 */
	public double calculateRecommendationScore(AnalyzeStudioRequest request) {
		// 1. Период в днях
		long epochSecStart = request.getStartDate().getSeconds();
		long epochSecEnd   = request.getEndDate().getSeconds();
		LocalDate start = Instant.ofEpochSecond(epochSecStart).atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate end   = Instant.ofEpochSecond(epochSecEnd).atZone(ZoneId.systemDefault()).toLocalDate();
		long days = ChronoUnit.DAYS.between(start, end);
		days = Math.max(days, 1);

		// 2. Нормализация показателей
		int totalBookings = request.getTotalBooking();
		double sigmoid = getSigmoid(request, totalBookings, days);
		return 10.0 * sigmoid;
	}

	private static double getSigmoid(AnalyzeStudioRequest request, int totalBookings, long days) {
		long totalDuration = request.getTotalDurationSum();
		double price = request.getPricePerHour();

		double bookingsPerDay = (double) totalBookings / days;
		double avgDuration = totalBookings > 0 ?
				(double) totalDuration / totalBookings : 0;

		double B = Math.min(bookingsPerDay / 5.0, 1.0);   // макс. 5 броней/день
		double D = Math.min(avgDuration / 240.0, 1.0);    // макс. 4 часа средняя длительность
		double P = Math.max(0.0, 1.0 - price / 3000.0);   // цена в диапазоне 0-3000

		// 3. Веса и взвешенная сумма
		double weightedSum = 0.4 * B + 0.3 * D + 0.3 * P;

		// 4. Сигмоида с центром 0.5 и умножение на 10
		double k = 5.0;
		return 1.0 / (1.0 + Math.exp(-k * (weightedSum - 0.5)));
	}
}
