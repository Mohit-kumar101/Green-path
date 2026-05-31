package com.greenPath.Green_path.util;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import com.greenPath.Green_path.domain.GovernancePolicy;

/**
 * Host allow/block lists and HTTPS posture for enterprise link governance.
 */
public final class GovernanceTargetValidator {

	private GovernanceTargetValidator() {
	}

	public static void validateOrThrow(String rawUrl, GovernancePolicy policy, boolean globalRequireHttps) {
		if (rawUrl == null || rawUrl.isBlank()) {
			return;
		}
		URI uri;
		try {
			uri = URI.create(rawUrl.trim());
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Invalid URL for governance check.");
		}
		String host = uri.getHost();
		if (host == null || host.isBlank()) {
			throw new IllegalArgumentException("URL must include a host for governance check.");
		}
		String hostLower = host.toLowerCase(Locale.ROOT);

		boolean requireHttps = policy.getRequireHttpsForTargets() != null
				? policy.getRequireHttpsForTargets()
				: globalRequireHttps;
		if (requireHttps && !"https".equalsIgnoreCase(uri.getScheme())) {
			throw new IllegalArgumentException("Governance policy requires https targets.");
		}

		List<String> blocked = policy.getBlockedTargetHosts() != null ? policy.getBlockedTargetHosts() : List.of();
		for (String raw : blocked) {
			if (raw == null || raw.isBlank()) {
				continue;
			}
			String pattern = normalizePattern(raw);
			if (matchesHostSuffix(hostLower, pattern)) {
				throw new IllegalArgumentException("Target host is blocked by governance policy.");
			}
		}

		List<String> allowed = policy.getAllowedTargetHosts() != null ? policy.getAllowedTargetHosts() : List.of();
		if (!allowed.isEmpty()) {
			boolean ok = allowed.stream()
					.filter(s -> s != null && !s.isBlank())
					.map(GovernanceTargetValidator::normalizePattern)
					.anyMatch(p -> matchesHostSuffix(hostLower, p));
			if (!ok) {
				throw new IllegalArgumentException("Target host is not on the governance allow-list.");
			}
		}
	}

	private static String normalizePattern(String p) {
		return p.strip().toLowerCase(Locale.ROOT);
	}

	static boolean matchesHostSuffix(String hostLower, String pattern) {
		if (pattern.startsWith("*.")) {
			String suffix = pattern.substring(2);
			return hostLower.equals(suffix) || hostLower.endsWith("." + suffix);
		}
		return hostLower.equals(pattern) || hostLower.endsWith("." + pattern);
	}
}
