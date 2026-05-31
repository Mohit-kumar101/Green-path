package com.greenPath.Green_path.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseSettingsResponse {

	private boolean requireApprovalForNewLinks;

	private List<String> allowedTargetHosts;

	private List<String> blockedTargetHosts;

	private Integer clickRetentionDays;

	private Boolean requireHttpsForTargets;

	private String ssoIdpIssuerUri;

	private String enterpriseExternalId;
}
