package com.greenPath.Green_path.service;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.domain.Plan;
import com.greenPath.Green_path.dto.AuthResponse;
import com.greenPath.Green_path.exception.UnauthorizedException;
import com.greenPath.Green_path.dto.LoginRequest;
import com.greenPath.Green_path.dto.RegisterRequest;
import com.greenPath.Green_path.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

	private final AccountRepository accountRepository;

	public AuthResponse register(RegisterRequest request) {
		String email = request.getEmail().strip().toLowerCase();
		if (accountRepository.existsByEmailIgnoreCase(email)) {
			throw new IllegalArgumentException("An account with that email already exists.");
		}
		String apiKey = "sk_live_" + UUID.randomUUID().toString().replace("-", "");
		Account account = Account.builder()
				.email(email)
				.passwordHash(BCRYPT.encode(request.getPassword()))
				.apiKey(apiKey)
				.plan(Plan.FREE)
				.subscriptionStatus("ACTIVE")
				.billingPeriodStart(YearMonth.now(ZoneOffset.UTC).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant())
				.linksCreatedInPeriod(0)
				.companyName(request.getCompanyName() != null ? request.getCompanyName().strip() : null)
				.build();
		accountRepository.save(account);
		return AuthResponse.builder()
				.apiKey(apiKey)
				.email(email)
				.plan(Plan.FREE)
				.subscriptionStatus(account.getSubscriptionStatus())
				.message("Store your API key securely — it is not shown again. Use header X-API-Key on API calls.")
				.build();
	}

	public AuthResponse login(LoginRequest request) {
		String email = request.getEmail().strip().toLowerCase();
		Account account = accountRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new UnauthorizedException("Invalid email or password."));
		if (!BCRYPT.matches(request.getPassword(), account.getPasswordHash())) {
			throw new UnauthorizedException("Invalid email or password.");
		}
		return AuthResponse.builder()
				.apiKey(account.getApiKey())
				.email(account.getEmail())
				.plan(account.getPlan() != null ? account.getPlan() : Plan.FREE)
				.subscriptionStatus(account.getSubscriptionStatus())
				.message("Use X-API-Key on requests.")
				.build();
	}

	public Account requireByApiKey(String apiKey) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new UnauthorizedException("Missing X-API-Key.");
		}
		Account acc = accountRepository.findByApiKey(apiKey.strip())
				.orElseThrow(() -> new UnauthorizedException("Invalid API key."));
		if (!"ACTIVE".equals(acc.getSubscriptionStatus()) && !"TRIALING".equals(acc.getSubscriptionStatus())) {
			throw new UnauthorizedException("Subscription is not active. Update billing to continue.");
		}
		return acc;
	}
}
