package com.greenPath.Green_path.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class MongoUriEnvironmentPostProcessorTest {

	private final MongoUriEnvironmentPostProcessor processor = new MongoUriEnvironmentPostProcessor();

	@Test
	void usesFirstNonBlankEnvVar() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("MONGODB_URI", " ");
		env.setProperty("MONGO_URI", "mongodb+srv://cluster.example.net/greenpath");

		processor.postProcessEnvironment(env, new SpringApplication());

		assertThat(env.getProperty("spring.data.mongodb.uri"))
				.isEqualTo("mongodb+srv://cluster.example.net/greenpath");
	}

	@Test
	void skipsEmptyValuesAndFallsThroughToApplicationPropertiesDefault() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("MONGODB_URI", "");

		processor.postProcessEnvironment(env, new SpringApplication());

		assertThat(env.getProperty("spring.data.mongodb.uri")).isNull();
	}
}
