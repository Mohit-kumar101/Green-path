package com.greenPath.Green_path.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.domain.Plan;
import com.greenPath.Green_path.domain.ShortLink;
import com.greenPath.Green_path.dto.CreateLinkRequest;
import com.greenPath.Green_path.dto.CreateLinkResponse;
import com.greenPath.Green_path.repository.AccountRepository;
import com.greenPath.Green_path.repository.ClickEventRepository;
import com.greenPath.Green_path.repository.ShortLinkRepository;

@ExtendWith(MockitoExtension.class)
class ShortLinkServiceTest {

	@Mock
	private ShortLinkRepository shortLinkRepository;

	@Mock
	private ClickEventRepository clickEventRepository;

	@Mock
	private QrCodeService qrCodeService;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private QuotaService quotaService;

	@Mock
	private AuditLogService auditLogService;

	@InjectMocks
	private ShortLinkService shortLinkService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(shortLinkService, "publicBaseUrl", "http://localhost:8080");
		ReflectionTestUtils.setField(shortLinkService, "chaosProbability", 0.0);
		ReflectionTestUtils.setField(shortLinkService, "clickFingerprintSalt", "test");
		ReflectionTestUtils.setField(shortLinkService, "redirectTargetSafetyEnabled", false);
		ReflectionTestUtils.setField(shortLinkService, "urlRequireHttps", false);
		when(accountRepository.findById(anyString())).thenAnswer(inv -> Optional.of(Account.builder()
				.id(inv.getArgument(0))
				.plan(Plan.FREE)
				.build()));
	}

	@Test
	void create_rejectsDuplicateCustomCode() {
		when(shortLinkRepository.existsByShortCode("taken")).thenReturn(true);
		CreateLinkRequest req = new CreateLinkRequest();
		req.setTargetUrl("https://example.com");
		req.setCustomCode("taken");
		Account acc = Account.builder().id("a1").plan(Plan.FREE).build();
		assertThatThrownBy(() -> shortLinkService.create(req, acc, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("taken");
	}

	@Test
	void create_invokesQuotaAndPersistsAccount() {
		when(shortLinkRepository.existsByShortCode(anyString())).thenReturn(false);
		when(shortLinkRepository.save(any(ShortLink.class))).thenAnswer(invocation -> {
			ShortLink l = invocation.getArgument(0);
			l.setId("id1");
			return l;
		});
		CreateLinkRequest req = new CreateLinkRequest();
		req.setTargetUrl("https://example.com/hello");
		req.setHttpStatus(302);
		req.setRedirectDelaySeconds(0);
		req.setChaosMode(false);
		Account acc = Account.builder().id("acc1").plan(Plan.FREE).build();

		CreateLinkResponse res = shortLinkService.create(req, acc, null);

		verify(quotaService).assertCanCreateLink(acc);
		verify(quotaService).recordLinkCreated(acc);
		verify(auditLogService).record(anyString(), any(), anyString(), anyString(), any());
		assertThat(res.getShortUrl()).startsWith("http://localhost:8080/r/");
		assertThat(res.getManagementSecret()).isNotBlank();
	}
}
