package com.greenPath.Green_path.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MongoDeploymentDiagnosticsTest {

	@Test
	void hostHint_extractsAtlasHostWithoutCredentials() {
		assertThat(MongoDeploymentDiagnostics.hostHint(
				"mongodb+srv://user:secret@cluster0.abc123.mongodb.net/greenpath?retryWrites=true"))
				.isEqualTo("cluster0.abc123.mongodb.net");
	}

	@Test
	void hostHint_showsLocalhostDefault() {
		assertThat(MongoDeploymentDiagnostics.hostHint("mongodb://localhost:27017/greenpath"))
				.isEqualTo("localhost:27017");
	}

	@Test
	void hostHint_whenUnset() {
		assertThat(MongoDeploymentDiagnostics.hostHint(null)).isEqualTo("not-set");
	}
}
