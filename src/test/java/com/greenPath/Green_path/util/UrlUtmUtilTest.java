package com.greenPath.Green_path.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UrlUtmUtilTest {

	@Test
	void append_whenNoQuery_addsQuestionMark() {
		assertThat(UrlUtmUtil.appendUtmPreset("https://a.com/x", "utm_source=t"))
				.isEqualTo("https://a.com/x?utm_source=t");
	}

	@Test
	void append_whenHasQuery_usesAmpersand() {
		assertThat(UrlUtmUtil.appendUtmPreset("https://a.com/x?foo=1", "utm_source=t"))
				.isEqualTo("https://a.com/x?foo=1&utm_source=t");
	}

	@Test
	void append_blankPreset_returnsOriginal() {
		assertThat(UrlUtmUtil.appendUtmPreset("https://a.com", "  ")).isEqualTo("https://a.com");
	}
}
