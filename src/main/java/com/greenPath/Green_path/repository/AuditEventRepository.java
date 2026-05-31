package com.greenPath.Green_path.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.greenPath.Green_path.domain.AuditEvent;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {

	List<AuditEvent> findTop200ByAccountIdOrderByOccurredAtDesc(String accountId);
}
