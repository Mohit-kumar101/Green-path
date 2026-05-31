package com.greenPath.Green_path.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.greenPath.Green_path.domain.ShortLink;

public interface ShortLinkRepository extends MongoRepository<ShortLink, String> {

	Optional<ShortLink> findByShortCode(String shortCode);

	boolean existsByShortCode(String shortCode);

	long countByAccountIdAndActiveTrue(String accountId);

	List<ShortLink> findByAccountIdOrderByCreatedAtDesc(String accountId);

	List<ShortLink> findByAccountIdAndActiveTrueOrderByCreatedAtDesc(String accountId);
}
