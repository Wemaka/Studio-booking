package org.example.config;

import org.example.websocket.NotificationWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Конфигурация WebSocket — нативный Spring WebSocket.
 *
 * Регистрирует единственный endpoint: /ws/notifications
 * Клиент подключается через new WebSocket("ws://localhost:8084/ws/notifications")
 *
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final NotificationWebSocketHandler notificationHandler;

	public WebSocketConfig(NotificationWebSocketHandler notificationHandler) {
		this.notificationHandler = notificationHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry
				.addHandler(notificationHandler, "/ws/notifications")
				.setAllowedOrigins("*"); // в проде тут нужно указать домены
	}
}
