package com.greenPath.Green_path.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class MongoUriEnvironmentPostProcessorTest {

	private final MongoUriEnvironmentPostProcessor processor = new MongoUriEnvironmentPostProcessor();

	@Test
	void prefersMongoDbUriOverSpringAlias() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("SPRING_DATA_MONGODB_URI", "mongodb://localhost:27017/greenpath");
		env.setProperty("MONGODB_URI", "mongodb+srv://cluster.example.net/greenpath");

		processor.postProcessEnvironment(env, new SpringApplication());

		assertThat(env.getProperty("spring.mongodb.uri"))
				.isEqualTo("mongodb+srv://cluster.example.net/greenpath");
	}

	@Test
	void skipsBlankMongoDbUriAndUsesNextAlias() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("MONGODB_URI", " ");
		env.setProperty("MONGO_URI", "mongodb+srv://cluster.example.net/greenpath");

		processor.postProcessEnvironment(env, new SpringApplication());

		assertThat(env.getProperty("spring.mongodb.uri"))
				.isEqualTo("mongodb+srv://cluster.example.net/greenpath");
	}

	@Test
	void allowsMissingUriWhenLocalProfileActive() {
		MockEnvironment env = new MockEnvironment();
		env.setActiveProfiles("local");

		processor.postProcessEnvironment(env, new SpringApplication());

		assertThat(env.getProperty("spring.mongodb.uri")).isNull();
	}

	@Test
	void failsFastWhenUriMissingOutsideLocalProfile() {
		MockEnvironment env = new MockEnvironment();

		assertThatThrownBy(() -> processor.postProcessEnvironment(env, new SpringApplication()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("MONGODB_URI");
	}
}
