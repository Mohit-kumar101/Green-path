package com.greenPath.Green_path.web;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthInterceptor implements HandlerInterceptor {

	public static final String ACCOUNT_ATTR = "account";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final AuthService authService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (!requiresApiKey(request)) {
			return true;
		}
		String key = request.getHeader("X-API-Key");
		try {
			Account account = authService.requireByApiKey(key);
			request.setAttribute(ACCOUNT_ATTR, account);
			return true;
		}
		catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			MAPPER.writeValue(response.getWriter(), java.util.Map.of("error", ex.getMessage()));
			return false;
		}
	}

	private static boolean requiresApiKey(HttpServletRequest request) {
		String path = request.getRequestURI();
		String method = request.getMethod();
		if ("POST".equals(method) && path.startsWith("/api/links/") && linkApproveOrRejectPost(path)) {
			return true;
		}
		if ("GET".equals(method) && "/api/billing/plans".equals(path)) {
			return false;
		}
		if ("POST".equals(method) && "/api/auth/register".equals(path)) {
			return false;
		}
		if ("POST".equals(method) && "/api/auth/login".equals(path)) {
			return false;
		}
		if (path.startsWith("/api/auth/")) {
			return false;
		}
		if ("POST".equals(method) && "/api/links".equals(path)) {
			return true;
		}
		if (path.startsWith("/api/my/")) {
			return true;
		}
		if (path.startsWith("/api/account/")) {
			return true;
		}
		if (path.startsWith("/api/billing/")) {
			return true;
		}
		return false;
	}

	private static boolean linkApproveOrRejectPost(String path) {
		return path.endsWith("/approve") || path.endsWith("/reject");
	}
}
