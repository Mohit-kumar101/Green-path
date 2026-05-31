package com.greenPath.Green_path.service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.domain.Plan;
import com.greenPath.Green_path.dto.AccountMeResponse;
import com.greenPath.Green_path.repository.AccountRepository;
import com.greenPath.Green_path.repository.ShortLinkRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuotaService {

	private final AccountRepository accountRepository;
	private final ShortLinkRepository shortLinkRepository;

	@Transactional
	public void assertCanCreateLink(Account account) {
		Account fresh = reload(account.getId());
		rollBillingPeriodIfNeeded(fresh);
		fresh = reload(account.getId());
		Plan plan = planOf(fresh);
		long active = shortLinkRepository.countByAccountIdAndActiveTrue(fresh.getId());
		if (active >= plan.getMaxActiveLinks()) {
			throw new IllegalArgumentException("Active link limit reached for your plan (" + plan.getMaxActiveLinks() + "). Upgrade or delete old links.");
		}
		if (fresh.getLinksCreatedInPeriod() >= plan.getMaxCreatesPerMonth()) {
			throw new IllegalArgumentException("Monthly create limit reached (" + plan.getMaxCreatesPerMonth() + "). Upgrade or wait for the next billing period.");
		}
	}

	@Transactional
	public void recordLinkCreated(Account account) {
		Account fresh = reload(account.getId());
		rollBillingPeriodIfNeeded(fresh);
		fresh = reload(account.getId());
		fresh.setLinksCreatedInPeriod(fresh.getLinksCreatedInPeriod() + 1);
		accountRepository.save(fresh);
	}

	@Transactional
	public void rollBillingPeriodIfNeeded(Account account) {
		Instant now = Instant.now();
		if (account.getBillingPeriodStart() == null) {
			account.setBillingPeriodStart(YearMonth.now(ZoneOffset.UTC).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant());
			account.setLinksCreatedInPeriod(0);
			accountRepository.save(account);
			return;
		}
		YearMonth current = YearMonth.from(now.atZone(ZoneOffset.UTC));
		YearMonth stored = YearMonth.from(account.getBillingPeriodStart().atZone(ZoneOffset.UTC));
		if (!current.equals(stored)) {
			account.setBillingPeriodStart(YearMonth.now(ZoneOffset.UTC).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant());
			account.setLinksCreatedInPeriod(0);
			accountRepository.save(account);
		}
	}

	@Transactional
	public AccountMeResponse buildUsageSnapshot(Account account) {
		Account fresh = reload(account.getId());
		rollBillingPeriodIfNeeded(fresh);
		fresh = reload(account.getId());
		Plan plan = planOf(fresh);
		long active = shortLinkRepository.countByAccountIdAndActiveTrue(fresh.getId());
		return AccountMeResponse.builder()
				.email(fresh.getEmail())
				.plan(plan)
				.subscriptionStatus(fresh.getSubscriptionStatus())
				.companyName(fresh.getCompanyName())
				.activeLinks(active)
				.linksCreatedThisPeriod(fresh.getLinksCreatedInPeriod())
				.maxCreatesPerMonth(plan.getMaxCreatesPerMonth())
				.maxActiveLinks(plan.getMaxActiveLinks())
				.build();
	}

	private Account reload(String id) {
		return accountRepository.findById(id).orElseThrow();
	}

	private static Plan planOf(Account a) {
		return a.getPlan() != null ? a.getPlan() : Plan.FREE;
	}
}
