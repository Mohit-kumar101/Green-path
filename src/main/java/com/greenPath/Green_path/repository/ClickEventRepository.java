package com.greenPath.Green_path.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.greenPath.Green_path.domain.ClickEvent;

public interface ClickEventRepository extends MongoRepository<ClickEvent, String> {

	List<ClickEvent> findTop100ByShortCodeOrderByClickedAtDesc(String shortCode);

	long countByShortCode(String shortCode);
}
