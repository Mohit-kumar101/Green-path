package com.greenPath.Green_path.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.domain.AuditAction;
import com.greenPath.Green_path.domain.AuditEvent;
import com.greenPath.Green_path.domain.GovernancePolicy;
import com.greenPath.Green_path.dto.AuditEventResponse;
import com.greenPath.Green_path.dto.EnterpriseSettingsPutRequest;
import com.greenPath.Green_path.dto.EnterpriseSettingsResponse;
import com.greenPath.Green_path.repository.AccountRepository;
import com.greenPath.Green_path.repository.AuditEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnterpriseSettingsService {

	private final AccountRepository accountRepository;

	private final AuditEventRepository auditEventRepository;

	private final AuditLogService auditLogService;

	public EnterpriseSettingsResponse get(Account account) {
		Account fresh = accountRepository.findById(account.getId()).orElseThrow();
		GovernancePolicy g = fresh.effectiveGovernance();
		return EnterpriseSettingsResponse.builder()
				.requireApprovalForNewLinks(g.isRequireApprovalForNewLinks())
				.allowedTargetHosts(copy(g.getAllowedTargetHosts()))
				.blockedTargetHosts(copy(g.getBlockedTargetHosts()))
				.clickRetentionDays(g.getClickRetentionDays())
				.requireHttpsForTargets(g.getRequireHttpsForTargets())
				.ssoIdpIssuerUri(fresh.getSsoIdpIssuerUri())
				.enterpriseExternalId(fresh.getEnterpriseExternalId())
				.build();
	}

	@Transactional
	public EnterpriseSettingsResponse put(Account account, EnterpriseSettingsPutRequest req) {
		Account fresh = accountRepository.findById(account.getId()).orElseThrow();
		GovernancePolicy g = GovernancePolicy.builder()
				.requireApprovalForNewLinks(req.isRequireApprovalForNewLinks())
				.allowedTargetHosts(sanitizeHostList(req.getAllowedTargetHosts()))
				.blockedTargetHosts(sanitizeHostList(req.getBlockedTargetHosts()))
				.clickRetentionDays(req.getClickRetentionDays())
				.requireHttpsForTargets(req.getRequireHttpsForTargets())
				.build();
		fresh.setGovernance(g);
		fresh.setSsoIdpIssuerUri(trimToNull(req.getSsoIdpIssuerUri()));
		fresh.setEnterpriseExternalId(trimToNull(req.getEnterpriseExternalId()));
		accountRepository.save(fresh);
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("requireApprovalForNewLinks", g.isRequireApprovalForNewLinks());
		details.put("allowedCount", g.getAllowedTargetHosts().size());
		details.put("blockedCount", g.getBlockedTargetHosts().size());
		auditLogService.record(account.getId(), AuditAction.GOVERNANCE_UPDATED, "ACCOUNT", fresh.getId(), details);
		return get(fresh);
	}

	public List<AuditEventResponse> listRecentAudit(Account account) {
		return auditEventRepository.findTop200ByAccountIdOrderByOccurredAtDesc(account.getId()).stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	private AuditEventResponse toResponse(AuditEvent e) {
		return AuditEventResponse.builder()
				.id(e.getId())
				.action(e.getAction())
				.entityType(e.getEntityType())
				.entityId(e.getEntityId())
				.details(e.getDetails() != null ? new LinkedHashMap<>(e.getDetails()) : new LinkedHashMap<>())
				.occurredAt(e.getOccurredAt())
				.build();
	}

	private static List<String> copy(List<String> in) {
		return in == null ? List.of() : List.copyOf(in);
	}

	private static List<String> sanitizeHostList(List<String> raw) {
		if (raw == null) {
			return new ArrayList<>();
		}
		return raw.stream()
				.filter(StringUtils::hasText)
				.map(s -> s.strip().toLowerCase())
				.distinct()
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private static String trimToNull(String s) {
		if (!StringUtils.hasText(s)) {
			return null;
		}
		String t = s.strip();
		return t.isEmpty() ? null : t;
	}
}
