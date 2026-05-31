package com.greenPath.Green_path.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

final class MongoUriSources {

	/** MONGODB_URI first — avoid a blank/wrong SPRING_DATA_MONGODB_URI shadowing it on Render. */
	static final List<String> ENV_KEYS = List.of(
			"MONGODB_URI",
			"SPRING_DATA_MONGODB_URI",
			"MONGO_URI",
			"MONGO_URL",
			"DATABASE_URL");

	private static final List<Path> SECRET_FILE_PATHS = List.of(
			Path.of("/etc/secrets/MONGODB_URI"),
			Path.of("/etc/secrets/mongodb_uri"),
			Path.of("/run/secrets/MONGODB_URI"));

	private MongoUriSources() {
	}

	static Optional<String> resolve(ConfigurableEnvironmentAccessor environment) {
		for (String key : ENV_KEYS) {
			String value = trimToNull(environment.getProperty(key));
			if (value == null) {
				value = trimToNull(System.getenv(key));
			}
			if (value != null) {
				return Optional.of(value);
			}
		}
		for (Path path : SECRET_FILE_PATHS) {
			String value = readSecretFile(path);
			if (value != null) {
				return Optional.of(value);
			}
		}
		return Optional.empty();
	}

	static String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private static String readSecretFile(Path path) {
		try {
			if (!Files.isRegularFile(path)) {
				return null;
			}
			return trimToNull(Files.readString(path));
		} catch (IOException ignored) {
			return null;
		}
	}

	interface ConfigurableEnvironmentAccessor {
		String getProperty(String key);
	}
}
