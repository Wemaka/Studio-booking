package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * gRPC Enrichment Client — микросервис обогащения книг.
 *
 * Слушает событие book.created из RabbitMQ, вызывает gRPC-сервер
 * для аналитики и публикует book.enriched обратно в шину.
 *
 * Запуск:
 *   mvnw spring-boot:run -pl grpc-enrichment-client
 */
@SpringBootApplication
public class GrpcEnrichmentClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrpcEnrichmentClientApplication.class, args);
	}
}
