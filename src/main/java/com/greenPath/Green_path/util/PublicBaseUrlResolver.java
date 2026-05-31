package com.greenPath.Green_path.util;

import java.net.URI;

import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public final class PublicBaseUrlResolver {

	private PublicBaseUrlResolver() {
	}

	public static String resolve(String configuredBase, HttpServletRequest request) {
		String configured = stripTrailingSlash(configuredBase);
		if (configured != null && !isLoopback(configured)) {
			return configured;
		}
		if (request != null) {
			String fromRequest = fromRequest(request);
			if (fromRequest != null) {
				return fromRequest;
			}
		}
		return configured != null ? configured : "http://localhost:8080";
	}

	public static String buildShortUrl(String base, String code) {
		return stripTrailingSlash(base) + "/r/" + code;
	}

	private static String fromRequest(HttpServletRequest request) {
		String proto = headerOrDefault(request, "X-Forwarded-Proto", request.getScheme());
		String host = headerOrDefault(request, "X-Forwarded-Host", request.getHeader("Host"));
		if (!StringUtils.hasText(host)) {
			host = request.getServerName();
			int port = request.getServerPort();
			if (port > 0 && !isDefaultPort(proto, port)) {
				host = host + ":" + port;
			}
		}
		if (!StringUtils.hasText(host)) {
			return null;
		}
		return stripTrailingSlash(proto + "://" + host);
	}

	private static boolean isDefaultPort(String scheme, int port) {
		return ("http".equalsIgnoreCase(scheme) && port == 80)
				|| ("https".equalsIgnoreCase(scheme) && port == 443);
	}

	private static String headerOrDefault(HttpServletRequest request, String name, String fallback) {
		String value = request.getHeader(name);
		return StringUtils.hasText(value) ? value.split(",")[0].trim() : fallback;
	}

	static boolean isLoopback(String url) {
		try {
			URI uri = URI.create(url);
			String host = uri.getHost();
			if (host == null) {
				return false;
			}
			String normalized = host.toLowerCase();
			return "localhost".equals(normalized) || "127.0.0.1".equals(normalized) || "[::1]".equals(normalized);
		}
		catch (Exception ex) {
			return false;
		}
	}

	private static String stripTrailingSlash(String url) {
		if (url == null) {
			return null;
		}
		return url.replaceAll("/+$", "");
	}
}
