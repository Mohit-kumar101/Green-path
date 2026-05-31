package com.greenPath.Green_path.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Per-account link governance (marketing ops). Embedded on {@link Account}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernancePolicy {

	@Builder.Default
	private boolean requireApprovalForNewLinks = false;

	/** When non-empty, targets must match at least one entry (host or *.suffix). */
	@Builder.Default
	private List<String> allowedTargetHosts = new ArrayList<>();

	@Builder.Default
	private List<String> blockedTargetHosts = new ArrayList<>();

	/**
	 * Declared click retention window in days (for compliance language / future purge jobs).
	 */
	private Integer clickRetentionDays;

	/** When non-null, overrides global {@code app.url-require-https} for governance checks. */
	private Boolean requireHttpsForTargets;

	public static GovernancePolicy permissiveDefaults() {
		return GovernancePolicy.builder()
				.requireApprovalForNewLinks(false)
				.allowedTargetHosts(new ArrayList<>())
				.blockedTargetHosts(new ArrayList<>())
				.clickRetentionDays(null)
				.requireHttpsForTargets(null)
				.build();
	}
}
