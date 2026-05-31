package com.greenPath.Green_path.dto;

import java.time.Instant;

import com.greenPath.Green_path.domain.LinkApprovalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyLinkSummaryResponse {

	private String shortCode;

	private String shortUrl;

	private String targetUrl;

	private String title;

	private long clickCount;

	private boolean active;

	private Instant createdAt;

	private LinkApprovalStatus linkApprovalStatus;

	private boolean platformRoutingEnabled;
}
