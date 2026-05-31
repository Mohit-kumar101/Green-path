package com.greenPath.Green_path.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.dto.CreateLinkRequest;
import com.greenPath.Green_path.dto.CreateLinkResponse;
import com.greenPath.Green_path.dto.LinkStatsResponse;
import com.greenPath.Green_path.service.CreateRateLimiterService;
import com.greenPath.Green_path.service.ShortLinkService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class LinkApiController {

	public static final String MANAGE_TOKEN_HEADER = "X-Manage-Token";

	public static final String API_KEY_HEADER = "X-API-Key";

	private final ShortLinkService shortLinkService;

	private final CreateRateLimiterService createRateLimiterService;

	@PostMapping
	public ResponseEntity<CreateLinkResponse> create(
			@Valid @RequestBody CreateLinkRequest request,
			@RequestAttribute(ApiKeyAuthInterceptor.ACCOUNT_ATTR) Account account,
			HttpServletRequest httpRequest) {
		createRateLimiterService.check(account.getApiKey());
		return ResponseEntity.status(HttpStatus.CREATED).body(shortLinkService.create(request, account, httpRequest));
	}

	@GetMapping("/{code}/stats")
	public LinkStatsResponse stats(
			@PathVariable String code,
			@RequestHeader(value = MANAGE_TOKEN_HEADER, required = false) String manageToken,
			@RequestHeader(value = API_KEY_HEADER, required = false) String apiKey) {
		return shortLinkService.stats(code, manageToken, apiKey);
	}

	@GetMapping(value = "/{code}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> qr(@PathVariable String code, HttpServletRequest request) {
		byte[] png = shortLinkService.qrPng(code, request);
		return ResponseEntity.ok()
				.header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
				.body(png);
	}

	@DeleteMapping("/{code}")
	public ResponseEntity<Void> delete(
			@PathVariable String code,
			@RequestHeader(value = MANAGE_TOKEN_HEADER, required = false) String manageToken,
			@RequestHeader(value = API_KEY_HEADER, required = false) String apiKey) {
		shortLinkService.delete(code, manageToken, apiKey);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{code}/approve")
	public ResponseEntity<Void> approve(
			@PathVariable String code,
			@RequestAttribute(ApiKeyAuthInterceptor.ACCOUNT_ATTR) Account account) {
		shortLinkService.approve(code, account);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{code}/reject")
	public ResponseEntity<Void> reject(
			@PathVariable String code,
			@RequestAttribute(ApiKeyAuthInterceptor.ACCOUNT_ATTR) Account account) {
		shortLinkService.reject(code, account);
		return ResponseEntity.noContent().build();
	}
}
