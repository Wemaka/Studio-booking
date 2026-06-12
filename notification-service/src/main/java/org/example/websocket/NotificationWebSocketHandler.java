package org.example.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket-обработчик для рассылки уведомлений.
 *
 * Поддерживает потокобезопасный реестр подключённых сессий и
 * broadcast-метод для отправки JSON всем клиентам одновременно.
 *
 * Нативный Spring WebSocket.
 * Клиент использует стандартный браузерный API: new WebSocket(...).
 *
 * При подключении отправляет приветственное сообщение с количеством
 * активных подключений. При отправке обрабатывает обрывы (IOException)
 * и автоматически удаляет «мёртвые» сессии.
 */
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

	/**
	 * Потокобезопасный реестр активных WebSocket-сессий.
	 * ConcurrentHashMap.newKeySet() — Set на основе ConcurrentHashMap.
	 */
	private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
		log.info("WebSocket подключён: {} (всего: {})", session.getId(), sessions.size());

		// Приветственное сообщение — клиент узнаёт, что подключение активно
		String welcome = """
                {"type":"CONNECTED","message":"Подключено к Notification Service","activeConnections":%d}"""
				.formatted(sessions.size());
		session.sendMessage(new TextMessage(welcome));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		sessions.remove(session);
		log.info("WebSocket отключён: {} (причина: {}, осталось: {})",
				session.getId(), status, sessions.size());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		// Клиент может отправить ping — отвечаем pong
		String payload = message.getPayload();
		if ("ping".equalsIgnoreCase(payload.trim())) {
			session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
		log.warn("WebSocket transport error для {}: {}", session.getId(), exception.getMessage());
		sessions.remove(session);
	}

	/**
	 * Отправить JSON-сообщение всем подключённым клиентам.
	 *
	 * Обрабатывает IOException для каждой сессии отдельно —
	 * один «мёртвый» клиент не блокирует остальных.
	 *
	 * @param json строка JSON для отправки
	 */
	public void broadcast(String json) {
		if (sessions.isEmpty()) {
			return;
		}

		TextMessage message = new TextMessage(json);
		int sent = 0;
		int failed = 0;

		for (WebSocketSession session : sessions) {
			if (session.isOpen()) {
				try {
					session.sendMessage(message);
					sent++;
				} catch (IOException e) {
					log.warn("Ошибка отправки в сессию {}: {}", session.getId(), e.getMessage());
					sessions.remove(session);
					failed++;
				}
			} else {
				sessions.remove(session);
				failed++;
			}
		}

		log.debug("Broadcast: отправлено {}, ошибок {}", sent, failed);
	}

	/**
	 * Количество активных подключений (для мониторинга).
	 */
	public int getActiveConnectionCount() {
		return sessions.size();
	}
}
