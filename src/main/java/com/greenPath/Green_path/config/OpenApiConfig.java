package com.greenPath.Green_path.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI shrinkPathOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("ShrinkPath URL API")
						.version("1.0")
						.description("Short links with optional password, expiry, analytics, and safe redirect validation.")
						.license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
	}
}
