package com.greenPath.Green_path.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class PublicBaseUrlResolverTest {

	@Mock
	private HttpServletRequest request;

	@Test
	void resolve_usesConfiguredProductionUrl() {
		assertThat(PublicBaseUrlResolver.resolve("https://shrinkpath.onrender.com", null))
				.isEqualTo("https://shrinkpath.onrender.com");
	}

	@Test
	void resolve_prefersRequestHostWhenConfiguredIsLocalhost() {
		when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);
		when(request.getScheme()).thenReturn("http");
		when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
		when(request.getHeader("Host")).thenReturn("192.168.1.42:8080");

		assertThat(PublicBaseUrlResolver.resolve("http://localhost:8080", request))
				.isEqualTo("http://192.168.1.42:8080");
	}

	@Test
	void buildShortUrl_appendsRedirectPath() {
		assertThat(PublicBaseUrlResolver.buildShortUrl("https://example.com", "abc"))
				.isEqualTo("https://example.com/r/abc");
	}
}
