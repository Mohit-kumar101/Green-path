package com.greenPath.Green_path.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.domain.Plan;
import com.greenPath.Green_path.dto.PlanCatalogResponse;
import com.greenPath.Green_path.exception.UnauthorizedException;
import com.greenPath.Green_path.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

	public static final String BILLING_ADMIN_HEADER = "X-Billing-Admin-Secret";

	private final AccountRepository accountRepository;

	@Value("${app.billing-admin-secret:}")
	private String billingAdminSecret;

	@GetMapping("/plans")
	public PlanCatalogResponse plans() {
		List<PlanCatalogResponse.PlanRow> rows = List.of(
				row(Plan.FREE, "0"),
				row(Plan.STARTER, "9"),
				row(Plan.PRO, "29"));
		return PlanCatalogResponse.builder().plans(rows).build();
	}

	private static PlanCatalogResponse.PlanRow row(Plan p, String usd) {
		return PlanCatalogResponse.PlanRow.builder()
				.id(p)
				.name(p.getDisplayName())
				.maxActiveLinks(p.getMaxActiveLinks())
				.maxCreatesPerMonth(p.getMaxCreatesPerMonth())
				.suggestedMonthlyPriceUsd(usd)
				.build();
	}

	@PostMapping("/demo-upgrade")
	public ResponseEntity<Map<String, Object>> demoUpgrade(
			@RequestAttribute(ApiKeyAuthInterceptor.ACCOUNT_ATTR) Account account,
			@RequestHeader(BILLING_ADMIN_HEADER) String adminSecret,
			@RequestParam(defaultValue = "PRO") Plan plan) {
		if (!StringUtils.hasText(billingAdminSecret)) {
			throw new IllegalArgumentException("Billing admin secret is not configured (set app.billing-admin-secret).");
		}
		if (!billingAdminSecret.equals(adminSecret)) {
			throw new UnauthorizedException("Invalid billing admin secret.");
		}
		account.setPlan(plan);
		account.setSubscriptionStatus("ACTIVE");
		accountRepository.save(account);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("plan", plan.name());
		body.put("message", "Demo upgrade applied. In production, replace this with Stripe webhooks.");
		return ResponseEntity.ok(body);
	}

	@PostMapping("/checkout-session")
	public ResponseEntity<Map<String, String>> checkoutStub(
			@RequestAttribute(ApiKeyAuthInterceptor.ACCOUNT_ATTR) Account account) {
		return ResponseEntity.ok(Map.of(
				"message",
				"Stripe Checkout is not configured. For production: create a Checkout Session server-side, "
						+ "listen to webhooks, and set subscriptionStatus from billing events.",
				"accountEmail", account.getEmail()));
	}
}
