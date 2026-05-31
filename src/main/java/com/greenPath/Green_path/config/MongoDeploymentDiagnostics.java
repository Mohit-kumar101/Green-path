package com.greenPath.Green_path.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MongoDeploymentDiagnostics implements InfoContributor {

	private static final Logger log = LoggerFactory.getLogger(MongoDeploymentDiagnostics.class);

	private final String configuredUri;
	private final Map<String, Boolean> envVarPresence;
	private final String renderServiceName;
	private final String renderExternalUrl;

	public MongoDeploymentDiagnostics(
			@Value("${spring.mongodb.uri:}") String configuredUri,
			Environment environment) {
		this.configuredUri = configuredUri;
		this.envVarPresence = new LinkedHashMap<>();
		for (String key : MongoUriSources.ENV_KEYS) {
			envVarPresence.put(key, MongoUriSources.trimToNull(environment.getProperty(key)) != null);
		}
		this.renderServiceName = environment.getProperty("RENDER_SERVICE_NAME", "");
		this.renderExternalUrl = environment.getProperty("RENDER_EXTERNAL_URL", "");
	}

	@EventListener(ApplicationReadyEvent.class)
	public void logMongoConfigOnStartup() {
		String hostHint = hostHint(configuredUri);
		if (hostHint.contains("localhost") || "not-set".equals(hostHint)) {
			log.error(
					"MongoDB is using {} — Atlas URI was NOT loaded. "
							+ "On Render, open service '{}' ({}) → Environment → set MONGODB_URI (not a Secret File unless mounted). "
							+ "Env vars present (non-blank): {}",
					hostHint,
					renderServiceName.isBlank() ? "your web service" : renderServiceName,
					renderExternalUrl.isBlank() ? "see dashboard URL" : renderExternalUrl,
					envVarPresence);
		} else {
			log.info("MongoDB host: {} (Render service: {})", hostHint, renderServiceName);
		}
		if (!renderServiceName.isBlank()) {
			logRenderOutboundIpForAtlas();
		}
	}

	private void logRenderOutboundIpForAtlas() {
		try {
			HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://api.ipify.org"))
					.timeout(Duration.ofSeconds(5))
					.GET()
					.build();
			String ip = client.send(request, HttpResponse.BodyHandlers.ofString()).body().trim();
			log.info(
					"Render outbound IP (for Atlas Network Access): {} — also add all CIDR ranges from "
							+ "Dashboard → your service → Connect → Outbound, or use 0.0.0.0/0 while testing.",
					ip);
		} catch (Exception e) {
			log.warn("Could not detect Render outbound IP for Atlas allowlist: {}", e.getMessage());
		}
	}

	@Override
	public void contribute(Info.Builder builder) {
		String hostHint = hostHint(configuredUri);
		builder.withDetail("mongodbConfig", Map.of(
				"host", hostHint,
				"acceptedEnvVars", MongoUriSources.ENV_KEYS,
				"envVarSet", envVarPresence,
				"usingLocalhostDefault", hostHint.contains("localhost") || "not-set".equals(hostHint),
				"renderServiceName", renderServiceName,
				"renderExternalUrl", renderExternalUrl));
	}

	static String hostHint(String uri) {
		String trimmed = MongoUriSources.trimToNull(uri);
		if (trimmed == null) {
			return "not-set";
		}
		String rest = trimmed.replaceFirst("^mongodb(\\+srv)?://", "");
		int at = rest.lastIndexOf('@');
		if (at >= 0) {
			rest = rest.substring(at + 1);
		}
		int end = rest.length();
		for (char sep : new char[] { '/', '?' }) {
			int idx = rest.indexOf(sep);
			if (idx >= 0) {
				end = Math.min(end, idx);
			}
		}
		return rest.substring(0, end);
	}
}
