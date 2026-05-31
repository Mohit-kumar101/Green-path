package com.greenPath.Green_path.util;

public enum UserAgentPlatform {
	IOS,
	ANDROID,
	DESKTOP;

	public static UserAgentPlatform detect(String userAgent) {
		if (userAgent == null || userAgent.isBlank()) {
			return DESKTOP;
		}
		String u = userAgent.toLowerCase();
		if (u.contains("iphone") || u.contains("ipad") || u.contains("ipod")) {
			return IOS;
		}
		if (u.contains("android")) {
			return ANDROID;
		}
		return DESKTOP;
	}
}
