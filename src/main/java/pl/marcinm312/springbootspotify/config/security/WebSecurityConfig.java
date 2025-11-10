package pl.marcinm312.springbootspotify.config.security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@RequiredArgsConstructor
	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfig {

		private final RestTemplate restTemplate;

		@Bean
		SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

			http.securityMatcher("/api/**") // tylko REST API
					.authorizeHttpRequests(auth -> auth

							.requestMatchers(
									"/api/public/**"
							)
							.permitAll()

							.anyRequest().authenticated())

					.sessionManagement(sessionManagement -> sessionManagement
							.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

					.oauth2ResourceServer(oauth2 -> oauth2
							.bearerTokenResolver(request -> {
								String authHeader = request.getHeader("Authorization");
								if (authHeader != null && authHeader.startsWith("Bearer ")) {
									return authHeader.substring(7);
								}
								return null; // brak tokenu => pozwala przejść do permitAll
							})
							.authenticationEntryPoint(new CustomAuthenticationEntryPoint())
							.opaqueToken(opaque -> opaque.introspector(spotifyIntrospector()))
					)

					.exceptionHandling(exceptionHandling -> exceptionHandling
							.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
					.csrf(AbstractHttpConfigurer::disable);

			return http.build();
		}

		private OpaqueTokenIntrospector spotifyIntrospector() {

			return token -> {
				HttpHeaders headers = new HttpHeaders();
				headers.setBearerAuth(token);

				ResponseEntity<Map<String, Object>> response;
				try {
					response = restTemplate.exchange(
							"https://api.spotify.com/v1/me",
							HttpMethod.GET,
							new HttpEntity<>(headers),
							new ParameterizedTypeReference<>() {}
					);
				} catch (Exception e) {
					throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"),
							"Spotify token introspection failed");
				}

				if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
					throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"),
							"Spotify token introspection failed");
				}

				Map<String, Object> userInfo = response.getBody();
				String userId = (String) userInfo.get("id");

				return new OAuth2AuthenticatedPrincipal() {
					@Override
					public Map<String, Object> getAttributes() {
						return userInfo;
					}

					@Override
					public Collection<? extends GrantedAuthority> getAuthorities() {
						return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
					}

					@Override
					public String getName() {
						return userId != null ? userId : "unknown";
					}
				};
			};
		}
	}

	@RequiredArgsConstructor
	@Configuration
	@Order(2)
	public static class AppWebSecurityConfig {

		private final ClientRegistrationRepository clientRegistrationRepository;

		@Bean
		public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

			DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(this.clientRegistrationRepository, "/oauth2/authorization");
			authorizationRequestResolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());

			http.securityMatcher("/**")
					.authorizeHttpRequests(authorizeRequests -> authorizeRequests

							.requestMatchers(

									"/",
									"/error",
									"/favicon.ico",

									//CSS
									"/css/**",

									//JS
									"/js/**",

									//SWAGGER
									"/swagger/**",
									"/swagger-ui/**",
									"/swagger-ui.html",
									"/webjars/**",
									"/swagger-resources/**",
									"/configuration/**",
									"/v3/api-docs/**"

							)
							.permitAll()
							.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()

							.requestMatchers(
									"/app/**"
							)
							.authenticated()
							.anyRequest().denyAll()
					)

					.oauth2Login(
							oauth2 -> oauth2
									.permitAll()
									.authorizationEndpoint(authorization -> authorization
											.authorizationRequestResolver(authorizationRequestResolver))
									.failureHandler(new CustomOAuth2FailureHandler())
					)
					.logout(logout -> logout
							.permitAll()
							.logoutSuccessUrl("/")
							.logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/logout"))
					)

					.sessionManagement(
							sessionManagement -> sessionManagement
									.maximumSessions(10000)
									.maxSessionsPreventsLogin(false)
									.expiredUrl("/oauth2/authorization/spotify")
									.sessionRegistry(sessionRegistry()));

			return http.build();
		}

		@Bean
		SessionRegistry sessionRegistry() {
			return new SessionRegistryImpl();
		}

		@Bean
		public static ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
			return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
		}
	}
}
