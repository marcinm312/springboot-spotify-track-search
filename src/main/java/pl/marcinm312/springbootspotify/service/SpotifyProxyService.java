package pl.marcinm312.springbootspotify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pl.marcinm312.springbootspotify.model.security.TestTokenData;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class SpotifyProxyService {

	private final RestTemplate restTemplate;
	private final Environment environment;

	public ResponseEntity<TestTokenData> getTestToken() {

		String url = "https://accounts.spotify.com/api/token";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("grant_type", "client_credentials");
		formData.add("client_id", environment.getProperty("spring.security.oauth2.client.registration.spotify.client-id"));
		formData.add("client_secret", environment.getProperty("spring.security.oauth2.client.registration.spotify.client-secret"));

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

		ResponseEntity<TestTokenData> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				requestEntity,
				TestTokenData.class
		);

		return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
	}
}
