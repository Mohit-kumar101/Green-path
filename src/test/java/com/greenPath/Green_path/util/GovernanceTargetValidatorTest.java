package com.greenPath.Green_path.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.greenPath.Green_path.domain.GovernancePolicy;

class GovernanceTargetValidatorTest {

	@Test
	void allowList_blocksUnknownHost() {
		GovernancePolicy p = GovernancePolicy.builder()
				.allowedTargetHosts(List.of("partner.com"))
				.blockedTargetHosts(List.of())
				.build();
		assertThatThrownBy(() -> GovernanceTargetValidator.validateOrThrow("https://evil.com/x", p, false))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("allow-list");
	}

	@Test
	void allowList_allowsSubdomainMatch() {
		GovernancePolicy p = GovernancePolicy.builder()
				.allowedTargetHosts(List.of("partner.com"))
				.blockedTargetHosts(List.of())
				.build();
		assertThatCode(() -> GovernanceTargetValidator.validateOrThrow("https://app.partner.com/x", p, false))
				.doesNotThrowAnyException();
	}

	@Test
	void blockList_wins() {
		GovernancePolicy p = GovernancePolicy.builder()
				.allowedTargetHosts(List.of("partner.com", "evil.com"))
				.blockedTargetHosts(List.of("evil.com"))
				.build();
		assertThatThrownBy(() -> GovernanceTargetValidator.validateOrThrow("https://evil.com/x", p, false))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("blocked");
	}

	@Test
	void wildcardAllowedPattern() {
		GovernancePolicy p = GovernancePolicy.builder()
				.allowedTargetHosts(List.of("*.cdn.example"))
				.blockedTargetHosts(List.of())
				.build();
		assertThatCode(() -> GovernanceTargetValidator.validateOrThrow("https://a.b.cdn.example/path", p, false))
				.doesNotThrowAnyException();
	}

	@Test
	void matchesHostSuffix_exposedBehavior() {
		assertThat(GovernanceTargetValidator.matchesHostSuffix("www.partner.com", "partner.com")).isTrue();
		assertThat(GovernanceTargetValidator.matchesHostSuffix("partner.com", "partner.com")).isTrue();
		assertThat(GovernanceTargetValidator.matchesHostSuffix("other.com", "partner.com")).isFalse();
	}
}
