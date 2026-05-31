package com.greenPath.Green_path.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.greenPath.Green_path.domain.AuditAction;
import com.greenPath.Green_path.domain.AuditEvent;
import com.greenPath.Green_path.repository.AuditEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

	private final AuditEventRepository auditEventRepository;

	public void record(String accountId, AuditAction action, String entityType, String entityId,
			Map<String, Object> details) {
		Map<String, Object> copy = details != null ? new LinkedHashMap<>(details) : new LinkedHashMap<>();
		AuditEvent event = AuditEvent.builder()
				.id(UUID.randomUUID().toString())
				.accountId(accountId)
				.action(action)
				.entityType(entityType)
				.entityId(entityId)
				.details(copy)
				.occurredAt(Instant.now())
				.build();
		auditEventRepository.save(event);
	}
}
