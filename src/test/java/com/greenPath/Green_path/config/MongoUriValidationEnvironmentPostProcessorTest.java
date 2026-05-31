package com.greenPath.Green_path.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class MongoUriValidationEnvironmentPostProcessorTest {

	private final MongoUriValidationEnvironmentPostProcessor processor =
			new MongoUriValidationEnvironmentPostProcessor();

	@Test
	void allowsAtlasUriOnRender() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("RENDER", "true");
		env.setProperty("spring.mongodb.uri", "mongodb+srv://u:p@cluster0.abc.mongodb.net/greenpath");

		assertThatCode(() -> processor.postProcessEnvironment(env, new SpringApplication()))
				.doesNotThrowAnyException();
	}

	@Test
	void rejectsLocalhostOnRender() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("RENDER_SERVICE_NAME", "green-path");
		env.setProperty("spring.mongodb.uri", "mongodb://localhost:27017/greenpath");

		assertThatThrownBy(() -> processor.postProcessEnvironment(env, new SpringApplication()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("MONGODB_URI");
	}

	@Test
	void rejectsLocalProfileOnRender() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("RENDER", "true");
		env.setActiveProfiles("local");

		assertThatThrownBy(() -> processor.postProcessEnvironment(env, new SpringApplication()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("SPRING_PROFILES_ACTIVE");
	}
}
