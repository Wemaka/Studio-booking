package org.example.controller;

import org.example.model.AuditEntry;
import org.example.storage.AuditStorage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

	private final AuditStorage auditStorage;

	public AuditController(AuditStorage auditStorage) {
		this.auditStorage = auditStorage;
	}

	/**
	 * Возвращает последние аудит-записи.
	 *
	 * Пример: GET /api/audit?limit=50
	 */
	@GetMapping
	public Map<String, Object> getAuditLog(
			@RequestParam(defaultValue = "100") int limit) {

		List<AuditEntry> entries = auditStorage.findLatest(limit);
		List<AuditEntry> safeEntries = entries != null ? entries : List.of();

		return Map.of(
				"totalEntries", auditStorage.count(),
				"showing", safeEntries.size(),
				"entries", safeEntries
		);
	}
}

