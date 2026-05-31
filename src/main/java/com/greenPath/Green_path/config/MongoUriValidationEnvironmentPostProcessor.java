package com.greenPath.Green_path.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;

/**
 * Runs after all config (including {@code application-local.properties}) is loaded.
 * Blocks accidental localhost Mongo on Render and other non-local deployments.
 */
public class MongoUriValidationEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		boolean localProfile = environment.acceptsProfiles(Profiles.of("local"));
		boolean onRender = isRender(environment);

		if (onRender && localProfile) {
			throw new IllegalStateException(
					"SPRING_PROFILES_ACTIVE=local must not be set on Render. "
							+ "Remove it from Environment, set MONGODB_URI to your Atlas URI, and redeploy.");
		}

		String host = MongoDeploymentDiagnostics.hostHint(environment.getProperty("spring.data.mongodb.uri", ""));
		boolean localhost = host.contains("localhost") || host.contains("127.0.0.1") || "not-set".equals(host);

		if (!localhost) {
			return;
		}

		if (onRender) {
			throw new IllegalStateException(
					"MongoDB is still localhost on Render. In the web service Environment tab, set MONGODB_URI "
							+ "to your Atlas connection string (mongodb+srv://...), save, and redeploy. "
							+ "A local .env file is not used in production.");
		}

		if (!localProfile) {
			throw new IllegalStateException(
					"MongoDB URI resolves to localhost but no 'local' profile is active. "
							+ "Set MONGODB_URI (or use spring.profiles.active=local only on your machine).");
		}
	}

	private static boolean isRender(ConfigurableEnvironment environment) {
		if ("true".equalsIgnoreCase(environment.getProperty("RENDER"))) {
			return true;
		}
		return !environment.getProperty("RENDER_SERVICE_NAME", "").isBlank();
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
