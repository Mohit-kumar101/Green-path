package com.greenPath.Green_path.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.dto.MyLinkSummaryResponse;
import com.greenPath.Green_path.service.ShortLinkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyLinksController {

	private final ShortLinkService shortLinkService;

	@GetMapping("/links")
	public List<MyLinkSummaryResponse> myLinks(
			@RequestAttribute(ApiKeyAuthInterceptor.ACCOUNT_ATTR) Account account,
			HttpServletRequest request) {
		return shortLinkService.listMine(account, request);
	}
}
