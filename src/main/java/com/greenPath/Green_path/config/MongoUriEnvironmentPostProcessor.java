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
		MongoUriSources.resolve(environment::getProperty).ifPresent(uri -> {
			Map<String, Object> properties = new HashMap<>();
			properties.put("spring.data.mongodb.uri", uri);
			environment.getPropertySources().addFirst(new MapPropertySource("resolvedMongoUri", properties));
		});
	}
}
