package com.greenPath.Green_path.util;

public final class UrlUtmUtil {

	private UrlUtmUtil() {
	}

	public static String appendUtmPreset(String targetUrl, String utmPreset) {
		if (utmPreset == null || utmPreset.isBlank()) {
			return targetUrl;
		}
		String preset = utmPreset.startsWith("?") ? utmPreset.substring(1) : utmPreset;
		preset = preset.strip();
		if (preset.isEmpty()) {
			return targetUrl;
		}
		if (targetUrl.contains("?")) {
			String sep = targetUrl.endsWith("?") || targetUrl.endsWith("&") ? "" : "&";
			return targetUrl + sep + preset;
		}
		return targetUrl + "?" + preset;
	}
}
