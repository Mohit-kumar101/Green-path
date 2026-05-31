package com.greenPath.Green_path.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
@Document("accounts")
public class Account {

	@Id
	private String id;

	@Indexed(unique = true)
	private String email;

	private String passwordHash;

	@Indexed(unique = true)
	private String apiKey;

	private Plan plan;

	/** ACTIVE, PAST_DUE, CANCELLED — for Stripe-style billing later */
	private String subscriptionStatus;

	private Instant billingPeriodStart;

	private int linksCreatedInPeriod;

	private String companyName;

	/** Marketing-ops governance; null means permissive defaults. */
	private GovernancePolicy governance;

	/**
	 * Optional IdP issuer URI for SAML/OIDC federation (stored for enterprise onboarding;
	 * federation is not enforced by this OSS build).
	 */
	private String ssoIdpIssuerUri;

	/** Opaque customer reference from an external IdP or CRM. */
	private String enterpriseExternalId;

	public GovernancePolicy effectiveGovernance() {
		if (governance == null) {
			return GovernancePolicy.permissiveDefaults();
		}
		return GovernancePolicy.builder()
				.requireApprovalForNewLinks(governance.isRequireApprovalForNewLinks())
				.allowedTargetHosts(copyList(governance.getAllowedTargetHosts()))
				.blockedTargetHosts(copyList(governance.getBlockedTargetHosts()))
				.clickRetentionDays(governance.getClickRetentionDays())
				.requireHttpsForTargets(governance.getRequireHttpsForTargets())
				.build();
	}

	private static List<String> copyList(List<String> in) {
		return in == null ? new ArrayList<>() : new ArrayList<>(in);
	}
}
