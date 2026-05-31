package com.greenPath.Green_path.web;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.greenPath.Green_path.domain.LinkApprovalStatus;
import com.greenPath.Green_path.domain.ShortLink;
import com.greenPath.Green_path.service.ShortLinkService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/r")
@RequiredArgsConstructor
public class RedirectController {

	private final ShortLinkService shortLinkService;

	@GetMapping("/{code}")
	public ResponseEntity<?> get(
			@PathVariable String code,
			@RequestParam(name = "exec", defaultValue = "false") boolean exec,
			HttpSession session,
			HttpServletRequest request) {
		return handle(code, exec, session, request, null);
	}

	@PostMapping(value = "/{code}/unlock", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<?> unlock(
			@PathVariable String code,
			@RequestParam("password") String password,
			HttpSession session,
			HttpServletRequest request) {
		return handle(code, false, session, request, password);
	}

	private ResponseEntity<?> handle(
			String code,
			boolean exec,
			HttpSession session,
			HttpServletRequest request,
			String unlockPassword) {
		var opt = shortLinkService.findByCode(code);
		if (opt.isEmpty()) {
			return html(HttpStatus.NOT_FOUND, page("Not found", "<p>This short link does not exist.</p>"));
		}
		ShortLink link = opt.get();

		if (unlockPassword != null) {
			if (!shortLinkService.isLinkUsable(link)) {
				return html(HttpStatus.GONE, page("Link expired", "<p>This link is no longer active.</p>"));
			}
			if (!shortLinkService.isApprovedForPublicRedirect(link)) {
				LinkApprovalStatus st = link.getLinkApprovalStatus();
				if (st == LinkApprovalStatus.PENDING) {
					return html(HttpStatus.FORBIDDEN, page("Awaiting approval",
							"<p>This short link exists but is <strong>pending governance approval</strong> and cannot be used yet.</p>"));
				}
				return html(HttpStatus.GONE, page("Unavailable",
						"<p>This link is not available for public redirects.</p>"));
			}
			if (!shortLinkService.unlock(session, code, unlockPassword, link)) {
				return html(HttpStatus.OK, unlockPage(code, true));
			}
			return ResponseEntity.status(HttpStatus.FOUND)
					.header(HttpHeaders.LOCATION, "/r/" + code)
					.build();
		}

		if (!shortLinkService.isLinkUsable(link)) {
			return html(HttpStatus.GONE, page("Link expired", "<p>This link has expired, hit its click limit, or was disabled.</p>"));
		}

		if (!shortLinkService.isApprovedForPublicRedirect(link)) {
			LinkApprovalStatus st = link.getLinkApprovalStatus();
			if (st == LinkApprovalStatus.PENDING) {
				return html(HttpStatus.FORBIDDEN, page("Awaiting approval",
						"<p>This short link exists but is <strong>pending governance approval</strong> and cannot be used yet.</p>"));
			}
			return html(HttpStatus.GONE, page("Unavailable",
					"<p>This link is not available for public redirects.</p>"));
		}

		if (shortLinkService.needsPassword(link) && !shortLinkService.isUnlocked(session, code)) {
			return html(HttpStatus.OK, unlockPage(code, false));
		}

		if (!exec && link.getRedirectDelaySeconds() > 0) {
			String host = ShortLinkService.safeHostHint(link.getTargetUrl());
			return html(HttpStatus.OK, delayPage(code, link.getRedirectDelaySeconds(), host));
		}

		String target = shortLinkService.recordHitAndGetTarget(link, request);
		return ResponseEntity.status(shortLinkService.redirectHttpStatus(link))
				.location(URI.create(target))
				.build();
	}

	private static ResponseEntity<String> html(HttpStatus status, String body) {
		return ResponseEntity.status(status).contentType(MediaType.TEXT_HTML).body(body);
	}

	private static String page(String title, String inner) {
		return shell(title, inner);
	}

	private static String shell(String title, String inner) {
		return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>"
				+ ShortLinkService.escapeHtml(title)
				+ "</title><style>body{font-family:system-ui;background:#0f1419;color:#e8eef4;"
				+ "max-width:32rem;margin:3rem auto;padding:0 1rem}a{color:#5eead4}</style></head><body>"
				+ inner + "</body></html>";
	}

	private static String unlockPage(String code, boolean bad) {
		String msg = bad ? "<p style=\"color:#f87171\">Wrong password.</p>" : "";
		String form = "<h1>Password required</h1>"
				+ msg
				+ "<form method=\"post\" action=\"/r/" + ShortLinkService.escapeHtml(code) + "/unlock\">"
				+ "<label>Password<br><input name=\"password\" type=\"password\" required "
				+ "style=\"width:100%;padding:0.5rem;margin:0.5rem 0\"></label><br>"
				+ "<button type=\"submit\" style=\"padding:0.5rem 1rem\">Unlock</button></form>";
		return shell("Locked link", form);
	}

	private static String delayPage(String code, int seconds, String hostHint) {
		String safeCode = ShortLinkService.escapeHtml(code);
		String safeHost = ShortLinkService.escapeHtml(hostHint);
		String script = "<script>setTimeout(function(){location.replace('/r/" + safeCode + "?exec=true');},"
				+ (seconds * 1000L) + ");</script>";
		String body = "<h1>Redirecting…</h1><p>You are being sent to <strong>" + safeHost
				+ "</strong> in <span id=\"t\">" + seconds + "</span>s.</p>"
				+ "<noscript><p><a href=\"/r/" + safeCode + "?exec=true\">Continue</a></p></noscript>"
				+ script;
		return shell("Please wait", body);
	}
}
