package com.greenPath.Green_path.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLinkRequest {

	@NotBlank
	@URL
	private String targetUrl;

	private String customCode;

	@Size(max = 72)
	private String password;

	private String expiresAt; // ISO-8601 instant string, optional — parsed in service

	@Min(1)
	@Max(1_000_000)
	private Integer maxClicks;

	@Min(301)
	@Max(302)
	private int httpStatus = 302;

	@Size(max = 512)
	private String utmPreset;

	@Min(0)
	@Max(120)
	private int redirectDelaySeconds;

	private boolean chaosMode;

	@Size(max = 120)
	private String title;

	/** When true, redirect destination is chosen by user-agent with per-platform fallbacks. */
	private boolean platformRoutingEnabled;

	@Size(max = 2048)
	private String iosTargetUrl;

	@Size(max = 2048)
	private String androidTargetUrl;

	@Size(max = 2048)
	private String desktopTargetUrl;
}
