package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * gRPC Analytics Server — микросервис для аналитики книг.
 *
 * Запускает Spring Boot приложение и gRPC-сервер на порту 9090.
 * HTTP-порт (8083) используется для actuator/health endpoints.
 *
 * Запуск:
 *   mvnw spring-boot:run -pl grpc-analytics-server
 */
@SpringBootApplication
public class GrpcAnalyticsServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrpcAnalyticsServerApplication.class, args);
	}
}
