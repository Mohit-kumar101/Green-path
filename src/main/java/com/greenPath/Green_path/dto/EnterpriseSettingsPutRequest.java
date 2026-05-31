package com.greenPath.Green_path.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnterpriseSettingsPutRequest {

	private boolean requireApprovalForNewLinks;

	@Size(max = 64)
	private List<@Size(max = 253) String> allowedTargetHosts = new ArrayList<>();

	@Size(max = 64)
	private List<@Size(max = 253) String> blockedTargetHosts = new ArrayList<>();

	@Min(1)
	@Max(3650)
	private Integer clickRetentionDays;

	private Boolean requireHttpsForTargets;

	@Size(max = 512)
	private String ssoIdpIssuerUri;

	@Size(max = 256)
	private String enterpriseExternalId;
}
