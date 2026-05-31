package com.greenPath.Green_path.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.dto.AccountMeResponse;
import com.greenPath.Green_path.service.QuotaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

	private final QuotaService quotaService;

	@GetMapping("/me")
	public AccountMeResponse me(@RequestAttribute(ApiKeyAuthInterceptor.ACCOUNT_ATTR) Account account) {
		return quotaService.buildUsageSnapshot(account);
	}
}
