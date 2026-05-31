package com.greenPath.Green_path.config;

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
			@Value("${spring.data.mongodb.uri:}") String configuredUri,
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
