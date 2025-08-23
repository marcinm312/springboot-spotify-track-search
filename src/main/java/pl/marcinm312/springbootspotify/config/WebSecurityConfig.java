package pl.marcinm312.springbootspotify.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

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

		@Bean
		SecurityFilterChain apiFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {

			http.securityMatcher("/api/**") // tylko REST API
					.authorizeHttpRequests(auth -> auth

							.requestMatchers(
									new MvcRequestMatcher(introspector, "/api/getTestToken"),
									new MvcRequestMatcher(introspector, "/api/oauth2/introspect")
							)
							.permitAll()

							.anyRequest().authenticated())

					.sessionManagement(sessionManagement -> sessionManagement
							.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

					//.addFilterBefore(new BearerTokenPresenceFilter(), UsernamePasswordAuthenticationFilter.class)

					.oauth2ResourceServer(oauth2 -> oauth2
							.bearerTokenResolver(request -> {
								String authHeader = request.getHeader("Authorization");
								if (authHeader != null && authHeader.startsWith("Bearer ")) {
									return authHeader.substring(7);
								}
								return null; // brak tokenu => pozwala przejść do permitAll
							})
							.opaqueToken(opaque -> opaque.introspector(spotifyIntrospector()))
					)

					.exceptionHandling(exceptionHandling -> exceptionHandling
							.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
					.csrf(AbstractHttpConfigurer::disable);

			return http.build();
		}

		private OpaqueTokenIntrospector spotifyIntrospector() {

			return token -> {
				RestTemplate restTemplate = new RestTemplate();
				HttpHeaders headers = new HttpHeaders();
				headers.setBearerAuth(token);

				ResponseEntity<Map> response = restTemplate.exchange(
						"https://api.spotify.com/v1/me",
						HttpMethod.GET,
						new HttpEntity<>(headers),
						Map.class
				);

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
		public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {

			DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(this.clientRegistrationRepository, "/oauth2/authorization");
			authorizationRequestResolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());

			http.securityMatcher("/**")
					.authorizeHttpRequests(authorizeRequests -> authorizeRequests

							.requestMatchers(

									new MvcRequestMatcher(introspector, "/"),
									new MvcRequestMatcher(introspector, "/error"),
									new MvcRequestMatcher(introspector, "error/"),
									new MvcRequestMatcher(introspector, "/favicon.ico"),

									//CSS
									new MvcRequestMatcher(introspector, "/css/style.css"),

									//SWAGGER
									new MvcRequestMatcher(introspector, "/swagger/**"),
									new MvcRequestMatcher(introspector, "/swagger-ui/**"),
									new MvcRequestMatcher(introspector, "/swagger-ui.html"),
									new MvcRequestMatcher(introspector, "/webjars/**"),
									new MvcRequestMatcher(introspector, "/swagger-resources/**"),
									new MvcRequestMatcher(introspector, "/configuration/**"),
									new MvcRequestMatcher(introspector, "/v3/api-docs/**")

							)
							.permitAll()
							.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()

							.requestMatchers(
									new MvcRequestMatcher(introspector, "/app/**")
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
					.logout(logout -> logout.permitAll().logoutSuccessUrl("/").logoutRequestMatcher(new AntPathRequestMatcher("/logout")))

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
