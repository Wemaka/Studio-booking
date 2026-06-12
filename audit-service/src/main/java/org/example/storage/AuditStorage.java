package org.example.storage;

import org.example.model.AuditEntry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AuditStorage {

	private final ConcurrentLinkedDeque<AuditEntry> entries = new ConcurrentLinkedDeque<>();
	private final AtomicLong sequence = new AtomicLong(0);

	// Множество обработанных eventId — для дедупликации (idempotent consumer)
	private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

	/**
	 * Проверяет, было ли событие уже обработано.
	 *
	 * Idempotent consumer — ключевой паттерн в событийных системах.
	 * RabbitMQ гарантирует «at least once» доставку, но НЕ гарантирует
	 * «exactly once». Повторная доставка возможна при:
	 * - перезапуске consumer'а до отправки ACK,
	 * - потере соединения после обработки, но до подтверждения.
	 *
	 * @return true если событие уже было обработано (дубликат)
	 */
	public boolean isDuplicate(String eventId) {
		return !processedEventIds.add(eventId);
	}

	/**
	 * Сохраняет аудит-запись. Присваивает порядковый номер, добавляет в начало списка.
	 */
	public AuditEntry save(AuditEntry entry) {
		AuditEntry numbered = new AuditEntry(
				sequence.incrementAndGet(),
				entry.eventId(),
				entry.eventType(),
				entry.source(),
				entry.eventTimestamp(),
				entry.receivedAt(),
				entry.description()
		);
		entries.addFirst(numbered);
		return numbered;
	}

	/**
	 * Возвращает последние N записей (новые — первые).
	 */
	public List<AuditEntry> findLatest(int limit) {
		return entries.stream()
				.limit(limit)
				.toList();
	}

	/**
	 * Общее количество записей в журнале.
	 */
	public int count() {
		return entries.size();
	}
}

