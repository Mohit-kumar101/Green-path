package com.greenPath.Green_path.util;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Reduces open-redirect / SSRF-style abuse by only allowing http(s) targets whose resolved
 * addresses are not private, loopback, link-local, or multicast.
 */
public final class SafeTargetUrlValidator {

	private SafeTargetUrlValidator() {
	}

	public static void validateOrThrow(String rawUrl, boolean requireHttps) {
		if (rawUrl == null || rawUrl.isBlank()) {
			throw new IllegalArgumentException("Target URL is required.");
		}
		URI uri;
		try {
			uri = URI.create(rawUrl.trim());
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Invalid URL.");
		}
		String scheme = uri.getScheme();
		if (scheme == null) {
			throw new IllegalArgumentException("URL must include a scheme (https://…).");
		}
		if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
			throw new IllegalArgumentException("Only http and https URLs are allowed.");
		}
		if (requireHttps && !"https".equalsIgnoreCase(scheme)) {
			throw new IllegalArgumentException("Only https URLs are allowed when app.url-require-https=true.");
		}
		String host = uri.getHost();
		if (host == null || host.isBlank()) {
			throw new IllegalArgumentException("URL must include a host.");
		}
		if (host.equals("localhost") || host.endsWith(".local")) {
			throw new IllegalArgumentException("That host is not allowed for short links.");
		}
		try {
			InetAddress[] addresses = InetAddress.getAllByName(host);
			for (InetAddress addr : addresses) {
				if (addr.isAnyLocalAddress()
						|| addr.isLoopbackAddress()
						|| addr.isLinkLocalAddress()
						|| addr.isSiteLocalAddress()
						|| addr.isMulticastAddress()) {
					throw new IllegalArgumentException("That host resolves to a non-public address.");
				}
			}
		}
		catch (UnknownHostException e) {
			throw new IllegalArgumentException("Could not resolve host: " + host);
		}
	}
}
