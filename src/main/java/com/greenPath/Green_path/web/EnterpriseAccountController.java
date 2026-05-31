package com.greenPath.Green_path.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.dto.AuditEventResponse;
import com.greenPath.Green_path.dto.EnterpriseSettingsPutRequest;
import com.greenPath.Green_path.dto.EnterpriseSettingsResponse;
import com.greenPath.Green_path.service.EnterpriseSettingsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class EnterpriseAccountController {

	private final EnterpriseSettingsService enterpriseSettingsService;

	@GetMapping("/enterprise-settings")
	public EnterpriseSettingsResponse getEnterpriseSettings(
			@RequestAttribute(ApiKeyAuthInterceptor.ACCOUNT_ATTR) Account account) {
		return enterpriseSettingsService.get(account);
	}

	@PutMapping("/enterprise-settings")
	public EnterpriseSettingsResponse putEnterpriseSettings(
			@RequestAttribute(ApiKeyAuthInterceptor.ACCOUNT_ATTR) Account account,
			@Valid @RequestBody EnterpriseSettingsPutRequest body) {
		return enterpriseSettingsService.put(account, body);
	}

	@GetMapping("/audit-events")
	public List<AuditEventResponse> auditEvents(
			@RequestAttribute(ApiKeyAuthInterceptor.ACCOUNT_ATTR) Account account) {
		return enterpriseSettingsService.listRecentAudit(account);
	}
}
