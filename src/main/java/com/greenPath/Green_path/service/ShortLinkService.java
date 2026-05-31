package com.greenPath.Green_path.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.greenPath.Green_path.domain.Account;
import com.greenPath.Green_path.domain.AuditAction;
import com.greenPath.Green_path.domain.ClickEvent;
import com.greenPath.Green_path.domain.GovernancePolicy;
import com.greenPath.Green_path.domain.LinkApprovalStatus;
import com.greenPath.Green_path.domain.ShortLink;
import com.greenPath.Green_path.dto.CreateLinkRequest;
import com.greenPath.Green_path.dto.CreateLinkResponse;
import com.greenPath.Green_path.dto.LinkStatsResponse;
import com.greenPath.Green_path.dto.MyLinkSummaryResponse;
import com.greenPath.Green_path.exception.UnauthorizedException;
import com.greenPath.Green_path.repository.AccountRepository;
import com.greenPath.Green_path.repository.ClickEventRepository;
import com.greenPath.Green_path.repository.ShortLinkRepository;
import com.greenPath.Green_path.util.GovernanceTargetValidator;
import com.greenPath.Green_path.util.SafeTargetUrlValidator;
import com.greenPath.Green_path.util.PublicBaseUrlResolver;
import com.greenPath.Green_path.util.ShortCodeGenerator;
import com.greenPath.Green_path.util.UrlUtmUtil;
import com.greenPath.Green_path.util.UserAgentPlatform;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShortLinkService {

	public static final String CHAOS_TARGET = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

	private static final String SESSION_UNLOCK_PREFIX = "LINK_UNLOCK_";
	private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
	private static final SecureRandom RANDOM = new SecureRandom();

	private final ShortLinkRepository shortLinkRepository;
	private final ClickEventRepository clickEventRepository;
	private final QrCodeService qrCodeService;
	private final AccountRepository accountRepository;
	private final QuotaService quotaService;
	private final AuditLogService auditLogService;

	@Value("${app.public-base-url:http://localhost:8080}")
	private String publicBaseUrl;

	@Value("${app.chaos-probability:0.12}")
	private double chaosProbability;

	@Value("${app.click-fingerprint-salt:greenpath-clicks}")
	private String clickFingerprintSalt;

	@Value("${app.url-require-https:false}")
	private boolean urlRequireHttps;

	@Value("${app.redirect-target-safety.enabled:true}")
	private boolean redirectTargetSafetyEnabled;

	@Transactional
	public CreateLinkResponse create(CreateLinkRequest request, Account account, HttpServletRequest httpRequest) {
		Account fresh = accountRepository.findById(account.getId()).orElseThrow();
		GovernancePolicy gov = fresh.effectiveGovernance();

		validateTargetUrl(request.getTargetUrl().trim(), gov);
		validateOptionalTargetUrl(trimOrNull(request.getIosTargetUrl()), gov);
		validateOptionalTargetUrl(trimOrNull(request.getAndroidTargetUrl()), gov);
		validateOptionalTargetUrl(trimOrNull(request.getDesktopTargetUrl()), gov);

		quotaService.assertCanCreateLink(account);
		String code = resolveCode(request.getCustomCode());
		Instant expiresAt = null;
		if (request.getExpiresAt() != null && !request.getExpiresAt().isBlank()) {
			expiresAt = Instant.parse(request.getExpiresAt());
		}
		String passwordHash = null;
		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			passwordHash = BCRYPT.encode(request.getPassword());
		}
		LinkApprovalStatus approval = gov.isRequireApprovalForNewLinks()
				? LinkApprovalStatus.PENDING
				: LinkApprovalStatus.APPROVED;
		ShortLink link = ShortLink.builder()
				.shortCode(code)
				.targetUrl(request.getTargetUrl().trim())
				.createdAt(Instant.now())
				.expiresAt(expiresAt)
				.passwordHash(passwordHash)
				.maxClicks(request.getMaxClicks())
				.clickCount(0)
				.httpStatus(request.getHttpStatus())
				.managementSecret(UUID.randomUUID().toString())
				.title(request.getTitle())
				.utmPreset(request.getUtmPreset())
				.redirectDelaySeconds(request.getRedirectDelaySeconds())
				.chaosMode(request.isChaosMode())
				.active(true)
				.accountId(account.getId())
				.linkApprovalStatus(approval)
				.platformRoutingEnabled(request.isPlatformRoutingEnabled())
				.iosTargetUrl(trimOrNull(request.getIosTargetUrl()))
				.androidTargetUrl(trimOrNull(request.getAndroidTargetUrl()))
				.desktopTargetUrl(trimOrNull(request.getDesktopTargetUrl()))
				.build();
		shortLinkRepository.save(link);
		quotaService.recordLinkCreated(account);
		Map<String, Object> auditDetails = new LinkedHashMap<>();
		auditDetails.put("shortCode", code);
		auditDetails.put("linkApprovalStatus", approval.name());
		auditDetails.put("platformRoutingEnabled", link.isPlatformRoutingEnabled());
		auditLogService.record(account.getId(), AuditAction.LINK_CREATED, "SHORT_LINK", code, auditDetails);
		String base = PublicBaseUrlResolver.resolve(publicBaseUrl, httpRequest);
		String shortUrl = PublicBaseUrlResolver.buildShortUrl(base, code);
		return CreateLinkResponse.builder()
				.id(link.getId())
				.shortCode(code)
				.shortUrl(shortUrl)
				.managementSecret(link.getManagementSecret())
				.qrUrl("/api/links/" + code + "/qr.png")
				.statsUrl(base + "/api/links/" + code + "/stats (GET with X-Manage-Token or X-API-Key as owner)")
				.linkApprovalStatus(approval)
				.platformRoutingEnabled(link.isPlatformRoutingEnabled())
				.build();
	}

	private void validateTargetUrl(String targetUrl, GovernancePolicy gov) {
		boolean https = effectiveRequireHttps(gov);
		if (redirectTargetSafetyEnabled) {
			SafeTargetUrlValidator.validateOrThrow(targetUrl, https);
		}
		GovernanceTargetValidator.validateOrThrow(targetUrl, gov, urlRequireHttps);
	}

	private void validateOptionalTargetUrl(String url, GovernancePolicy gov) {
		if (!StringUtils.hasText(url)) {
			return;
		}
		validateTargetUrl(url, gov);
	}

	private boolean effectiveRequireHttps(GovernancePolicy gov) {
		return gov.getRequireHttpsForTargets() != null ? gov.getRequireHttpsForTargets() : urlRequireHttps;
	}

	private static String trimOrNull(String s) {
		if (!StringUtils.hasText(s)) {
			return null;
		}
		String t = s.strip();
		return t.isEmpty() ? null : t;
	}

	private String resolveCode(String custom) {
		if (custom != null && !custom.isBlank()) {
			String c = custom.strip();
			if (!c.matches("[a-zA-Z0-9_-]{3,32}")) {
				throw new IllegalArgumentException("customCode must be 3-32 characters: letters, digits, _ or -.");
			}
			if (shortLinkRepository.existsByShortCode(c)) {
				throw new IllegalArgumentException("That short code is already taken.");
			}
			return c;
		}
		for (int attempt = 0; attempt < 12; attempt++) {
			String candidate = ShortCodeGenerator.randomCode(8);
			if (!shortLinkRepository.existsByShortCode(candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException("Could not allocate a unique short code.");
	}

	public Optional<ShortLink> findByCode(String code) {
		return shortLinkRepository.findByShortCode(code);
	}

	public boolean isApprovedForPublicRedirect(ShortLink link) {
		LinkApprovalStatus s = link.getLinkApprovalStatus();
		if (s == null) {
			return true;
		}
		return s == LinkApprovalStatus.APPROVED;
	}

	public boolean isLinkUsable(ShortLink link) {
		if (!link.isActive()) {
			return false;
		}
		if (link.getExpiresAt() != null && Instant.now().isAfter(link.getExpiresAt())) {
			return false;
		}
		if (link.getMaxClicks() != null && link.getClickCount() >= link.getMaxClicks()) {
			return false;
		}
		return true;
	}

	public boolean needsPassword(ShortLink link) {
		return link.getPasswordHash() != null;
	}

	public boolean isUnlocked(HttpSession session, String code) {
		return Boolean.TRUE.equals(session.getAttribute(SESSION_UNLOCK_PREFIX + code));
	}

	public boolean unlock(HttpSession session, String code, String password, ShortLink link) {
		if (link.getPasswordHash() == null) {
			return true;
		}
		if (password == null || !BCRYPT.matches(password, link.getPasswordHash())) {
			return false;
		}
		session.setAttribute(SESSION_UNLOCK_PREFIX + code, Boolean.TRUE);
		return true;
	}

	private String resolveBaseTargetUrl(ShortLink link, String userAgent) {
		if (!link.isPlatformRoutingEnabled()) {
			return link.getTargetUrl();
		}
		return switch (UserAgentPlatform.detect(userAgent)) {
			case IOS -> firstNonBlank(link.getIosTargetUrl(), link.getTargetUrl());
			case ANDROID -> firstNonBlank(link.getAndroidTargetUrl(), link.getTargetUrl());
			case DESKTOP -> firstNonBlank(link.getDesktopTargetUrl(), link.getTargetUrl());
		};
	}

	private static String firstNonBlank(String prefer, String fallback) {
		return StringUtils.hasText(prefer) ? prefer : fallback;
	}

	private String resolveRedirectTargetWithBase(ShortLink link, String baseUrl) {
		if (link.isChaosMode() && RANDOM.nextDouble() < chaosProbability) {
			return UrlUtmUtil.appendUtmPreset(CHAOS_TARGET, link.getUtmPreset());
		}
		return UrlUtmUtil.appendUtmPreset(baseUrl, link.getUtmPreset());
	}

	@Transactional
	public String recordHitAndGetTarget(ShortLink link, HttpServletRequest request) {
		String base = resolveBaseTargetUrl(link, request.getHeader("User-Agent"));
		String finalUrl = resolveRedirectTargetWithBase(link, base);
		clickEventRepository.save(ClickEvent.builder()
				.shortCode(link.getShortCode())
				.clickedAt(Instant.now())
				.userAgent(truncate(request.getHeader("User-Agent"), 400))
				.referer(truncate(request.getHeader("Referer"), 400))
				.ipFingerprint(fingerprintClient(request))
				.build());
		link.setClickCount(link.getClickCount() + 1);
		if (link.getMaxClicks() != null && link.getClickCount() >= link.getMaxClicks()) {
			link.setActive(false);
		}
		shortLinkRepository.save(link);
		return finalUrl;
	}

	@Transactional
	public void approve(String code, Account account) {
		ShortLink link = shortLinkRepository.findByShortCode(code)
				.orElseThrow(() -> new IllegalArgumentException("Unknown short code."));
		assertOwner(link, account);
		if (link.getLinkApprovalStatus() != LinkApprovalStatus.PENDING) {
			throw new IllegalArgumentException("Only pending links can be approved.");
		}
		link.setLinkApprovalStatus(LinkApprovalStatus.APPROVED);
		shortLinkRepository.save(link);
		auditLogService.record(account.getId(), AuditAction.LINK_APPROVED, "SHORT_LINK", code, Map.of());
	}

	@Transactional
	public void reject(String code, Account account) {
		ShortLink link = shortLinkRepository.findByShortCode(code)
				.orElseThrow(() -> new IllegalArgumentException("Unknown short code."));
		assertOwner(link, account);
		if (link.getLinkApprovalStatus() != LinkApprovalStatus.PENDING) {
			throw new IllegalArgumentException("Only pending links can be rejected.");
		}
		link.setLinkApprovalStatus(LinkApprovalStatus.REJECTED);
		link.setActive(false);
		shortLinkRepository.save(link);
		auditLogService.record(account.getId(), AuditAction.LINK_REJECTED, "SHORT_LINK", code, Map.of());
	}

	private void assertOwner(ShortLink link, Account account) {
		if (!StringUtils.hasText(link.getAccountId()) || !link.getAccountId().equals(account.getId())) {
			throw new UnauthorizedException("Not the owner of this link.");
		}
	}

	private String fingerprintClient(HttpServletRequest request) {
		try {
			String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
					.map(v -> v.split(",")[0].strip())
					.orElse(request.getRemoteAddr());
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest((ip + "|" + clickFingerprintSalt).getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (Exception e) {
			return "";
		}
	}

	private static String truncate(String s, int max) {
		if (s == null) {
			return "";
		}
		return s.length() <= max ? s : s.substring(0, max);
	}

	public LinkStatsResponse stats(String code, String manageToken, String apiKey) {
		ShortLink link = shortLinkRepository.findByShortCode(code)
				.orElseThrow(() -> new IllegalArgumentException("Unknown short code."));
		assertStatsAuthorized(link, manageToken, apiKey);
		List<ClickEvent> recent = clickEventRepository.findTop100ByShortCodeOrderByClickedAtDesc(code);
		List<LinkStatsResponse.ClickRow> rows = recent.stream()
				.map(c -> LinkStatsResponse.ClickRow.builder()
						.clickedAt(c.getClickedAt())
						.userAgent(c.getUserAgent())
						.referer(c.getReferer())
						.build())
				.toList();
		return LinkStatsResponse.builder()
				.shortCode(link.getShortCode())
				.targetUrl(link.getTargetUrl())
				.totalClicks(clickEventRepository.countByShortCode(code))
				.active(link.isActive())
				.createdAt(link.getCreatedAt())
				.expiresAt(link.getExpiresAt())
				.maxClicks(link.getMaxClicks())
				.chaosMode(link.isChaosMode())
				.redirectDelaySeconds(link.getRedirectDelaySeconds())
				.linkApprovalStatus(link.getLinkApprovalStatus())
				.platformRoutingEnabled(link.isPlatformRoutingEnabled())
				.iosTargetUrl(link.getIosTargetUrl())
				.androidTargetUrl(link.getAndroidTargetUrl())
				.desktopTargetUrl(link.getDesktopTargetUrl())
				.recentClicks(rows)
				.build();
	}

	private void assertStatsAuthorized(ShortLink link, String manageToken, String apiKey) {
		if (StringUtils.hasText(manageToken) && manageToken.equals(link.getManagementSecret())) {
			return;
		}
		if (StringUtils.hasText(apiKey) && StringUtils.hasText(link.getAccountId())) {
			Account owner = accountRepository.findByApiKey(apiKey.strip()).orElse(null);
			if (owner != null && owner.getId().equals(link.getAccountId())) {
				return;
			}
		}
		throw new UnauthorizedException("Provide a valid X-Manage-Token or X-API-Key for the owning account.");
	}

	@Transactional
	public void delete(String code, String manageToken, String apiKey) {
		ShortLink link = shortLinkRepository.findByShortCode(code)
				.orElseThrow(() -> new IllegalArgumentException("Unknown short code."));
		boolean ok = StringUtils.hasText(manageToken) && manageToken.equals(link.getManagementSecret());
		if (!ok && StringUtils.hasText(apiKey) && StringUtils.hasText(link.getAccountId())) {
			ok = accountRepository.findByApiKey(apiKey.strip())
					.map(a -> a.getId().equals(link.getAccountId()))
					.orElse(false);
		}
		if (!ok) {
			throw new UnauthorizedException("Provide a valid X-Manage-Token or X-API-Key for the owning account.");
		}
		link.setActive(false);
		shortLinkRepository.save(link);
		if (StringUtils.hasText(link.getAccountId())) {
			auditLogService.record(link.getAccountId(), AuditAction.LINK_DELETED, "SHORT_LINK", code, Map.of());
		}
	}

	public byte[] qrPng(String code, HttpServletRequest request) {
		ShortLink link = shortLinkRepository.findByShortCode(code)
				.orElseThrow(() -> new IllegalArgumentException("Unknown short code."));
		String base = PublicBaseUrlResolver.resolve(publicBaseUrl, request);
		String shortUrl = PublicBaseUrlResolver.buildShortUrl(base, link.getShortCode());
		return qrCodeService.pngForText(shortUrl);
	}

	public List<MyLinkSummaryResponse> listMine(Account account, HttpServletRequest request) {
		String base = PublicBaseUrlResolver.resolve(publicBaseUrl, request);
		return shortLinkRepository.findByAccountIdAndActiveTrueOrderByCreatedAtDesc(account.getId()).stream()
				.map(l -> MyLinkSummaryResponse.builder()
						.shortCode(l.getShortCode())
						.shortUrl(base + "/r/" + l.getShortCode())
						.targetUrl(l.getTargetUrl())
						.title(l.getTitle())
						.clickCount(l.getClickCount())
						.active(l.isActive())
						.createdAt(l.getCreatedAt())
						.linkApprovalStatus(l.getLinkApprovalStatus())
						.platformRoutingEnabled(l.isPlatformRoutingEnabled())
						.build())
				.toList();
	}

	public static String safeHostHint(String url) {
		try {
			return URI.create(url).getHost();
		}
		catch (Exception e) {
			return "destination";
		}
	}

	public static String escapeHtml(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

	public HttpStatus redirectHttpStatus(ShortLink link) {
		return link.getHttpStatus() == 301 ? HttpStatus.MOVED_PERMANENTLY : HttpStatus.FOUND;
	}
}
