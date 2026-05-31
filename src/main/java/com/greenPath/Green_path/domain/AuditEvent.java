package com.greenPath.Green_path.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("audit_events")
@CompoundIndex(name = "account_occurred", def = "{'accountId': 1, 'occurredAt': -1}")
public class AuditEvent {

	@Id
	private String id;

	@Indexed
	private String accountId;

	private AuditAction action;

	private String entityType;

	private String entityId;

	@Builder.Default
	private Map<String, Object> details = new LinkedHashMap<>();

	@Indexed
	private Instant occurredAt;
}
