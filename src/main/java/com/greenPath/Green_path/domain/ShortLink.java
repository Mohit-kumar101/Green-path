package com.greenPath.Green_path.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("short_links")
public class ShortLink {

	@Id
	private String id;

	@Indexed(unique = true)
	private String shortCode;

	private String targetUrl;

	private Instant createdAt;

	/** When set, link stops working after this instant. */
	private Instant expiresAt;

	private String passwordHash;

	/** Maximum successful redirects; null = unlimited. */
	private Integer maxClicks;

	private long clickCount;

	/** 301 or 302. */
	private int httpStatus;

	/** Pass as X-Manage-Token to delete or read private stats. */
	private String managementSecret;

	private String title;

	/** Appended as query string (without leading ?). */
	private String utmPreset;

	/** HTML interstitial countdown before redirect (0 = immediate). */
	private int redirectDelaySeconds;

	/** Random surprise redirect with configured probability. */
	private boolean chaosMode;

	private boolean active;

	/** Owning SaaS account; null for legacy anonymous links. */
	private String accountId;

	/** Null or APPROVED = publicly redirectable when active. */
	private LinkApprovalStatus linkApprovalStatus;

	/**
	 * When true, redirects pick {@link #iosTargetUrl}, {@link #androidTargetUrl}, or
	 * {@link #desktopTargetUrl} by user-agent with fallback to {@link #targetUrl}.
	 */
	private boolean platformRoutingEnabled;

	private String iosTargetUrl;

	private String androidTargetUrl;

	private String desktopTargetUrl;
}
