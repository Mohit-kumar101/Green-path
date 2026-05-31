package com.greenPath.Green_path.dto;

import java.time.Instant;
import java.util.List;

import com.greenPath.Green_path.domain.LinkApprovalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkStatsResponse {

	private String shortCode;

	private String targetUrl;

	private long totalClicks;

	private boolean active;

	private Instant createdAt;

	private Instant expiresAt;

	private Integer maxClicks;

	private boolean chaosMode;

	private int redirectDelaySeconds;

	private LinkApprovalStatus linkApprovalStatus;

	private boolean platformRoutingEnabled;

	private String iosTargetUrl;

	private String androidTargetUrl;

	private String desktopTargetUrl;

	private List<ClickRow> recentClicks;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ClickRow {
		private Instant clickedAt;
		private String userAgent;
		private String referer;
	}
}
