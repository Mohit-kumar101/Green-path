package com.greenPath.Green_path.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SafeTargetUrlValidatorTest {

	@Test
	void rejects_javascriptScheme() {
		assertThatThrownBy(() -> SafeTargetUrlValidator.validateOrThrow("javascript:alert(1)", false))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("http");
	}

	@Test
	void rejects_localhost() {
		assertThatThrownBy(() -> SafeTargetUrlValidator.validateOrThrow("http://localhost/foo", false))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void rejects_httpsWhenRequireHttpsButHttpGiven() {
		assertThatThrownBy(() -> SafeTargetUrlValidator.validateOrThrow("http://example.com/", true))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("https");
	}
}
