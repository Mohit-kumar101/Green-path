package com.greenPath.Green_path.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Resolves Mongo URI from env vars or Render secret files before auto-configuration runs.
 */
public class MongoUriEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		var resolved = MongoUriSources.resolve(environment::getProperty);
		if (resolved.isPresent()) {
			Map<String, Object> properties = new HashMap<>();
			properties.put("spring.data.mongodb.uri", resolved.get());
			environment.getPropertySources().addFirst(new MapPropertySource("resolvedMongoUri", properties));
			return;
		}
		if (!isLocalProfile(environment)) {
			throw new IllegalStateException(
					"No MongoDB URI in the environment. Set MONGODB_URI on your host "
							+ "(Render: web service → Environment → Add variable). "
							+ "Accepted names: " + MongoUriSources.ENV_KEYS + ". "
							+ "Local dev: use profile 'local' or export MONGODB_URI.");
		}
	}

	private static boolean isLocalProfile(ConfigurableEnvironment environment) {
		return environment.acceptsProfiles(org.springframework.core.env.Profiles.of("local"));
	}
}
