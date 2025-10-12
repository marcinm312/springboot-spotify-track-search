package pl.marcinm312.springbootspotify.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customizeOpenAPI() {

		final String securitySchemeName = "Bearer Authentication";
		return new OpenAPI()
				.addSecurityItem(new SecurityRequirement()
						.addList(securitySchemeName))
				.components(new Components()
						.addSecuritySchemes(securitySchemeName, new SecurityScheme()
								.name(securitySchemeName)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("Opaque")
								.description("""
										Bearer token can be obtained by web browser:
										1. Go to: https://developer.spotify.com
										2. Open your web browser console and go to the Network tab
										3. Log in to your Spotify account
										4. In the browser console, find the request with the url: https://accounts.spotify.com/api/token
										5. Copy the access_token value from the JSON response body.
										""")));
	}

	@Bean
	public GroupedOpenApi publicApi() {

		return GroupedOpenApi.builder()
				.group("1. public-apis")
				.pathsToMatch("/api/**")
				.pathsToExclude("/api/actuator/**")
				.build();
	}

	@Bean
	public GroupedOpenApi actuatorApi() {

		return GroupedOpenApi.builder()
				.group("2. actuators")
				.pathsToMatch("/api/actuator/**")
				.build();
	}
}
