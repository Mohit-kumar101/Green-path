package com.greenPath.Green_path.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.greenPath.Green_path.domain.Account;

public interface AccountRepository extends MongoRepository<Account, String> {

	Optional<Account> findByEmailIgnoreCase(String email);

	Optional<Account> findByApiKey(String apiKey);

	boolean existsByEmailIgnoreCase(String email);
}
