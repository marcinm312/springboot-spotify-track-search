package pl.marcinm312.springbootspotify.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final ClientRegistrationRepository clientRegistrationRepository;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {

		DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(this.clientRegistrationRepository, "/oauth2/authorization");
		authorizationRequestResolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());

		http.authorizeHttpRequests(authorizeRequests -> authorizeRequests

						.requestMatchers(
								new MvcRequestMatcher(introspector, "/"),
								//CSS
								new MvcRequestMatcher(introspector, "/css/style.css")
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
