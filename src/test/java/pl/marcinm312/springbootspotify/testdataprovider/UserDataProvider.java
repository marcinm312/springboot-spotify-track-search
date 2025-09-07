package pl.marcinm312.springbootspotify.testdataprovider;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;

public class UserDataProvider {

	public static OAuth2AuthenticatedPrincipal getOAuth2AuthenticatedPrincipal() {

		Map<String, Object> userParams = getExampleUserParams();
		return new DefaultOAuth2AuthenticatedPrincipal(
				(String) userParams.get("id"),
				userParams,
				AuthorityUtils.createAuthorityList("ROLE_USER"));
	}

	public static OAuth2User getOAuth2User() {

		Map<String, Object> userParams = getExampleUserParams();
		return new DefaultOAuth2User(
				AuthorityUtils.createAuthorityList("ROLE_USER"),
				userParams,
				"id");
	}

	public static Map<String, Object> getExampleUserParams() {

		Map<String, Object> userParams = new HashMap<>();
		userParams.put("id", "01234user56789");
		userParams.put("display_name", "Jan Kowalski");
		userParams.put("email", "jan.kowalski@gmail.com");
		return userParams;
	}
}
