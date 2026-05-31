package com.greenPath.Green_path.dto;

import java.time.Instant;
import java.util.Map;

import com.greenPath.Green_path.domain.AuditAction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {

	private String id;

	private AuditAction action;

	private String entityType;

	private String entityId;

	private Map<String, Object> details;

	private Instant occurredAt;
}
