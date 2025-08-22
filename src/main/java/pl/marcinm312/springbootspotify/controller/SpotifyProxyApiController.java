package pl.marcinm312.springbootspotify.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinm312.springbootspotify.model.security.TestTokenData;
import pl.marcinm312.springbootspotify.service.SpotifyProxyService;

import java.util.Map;

@SecurityRequirements
@RequiredArgsConstructor
@RestController
public class SpotifyProxyApiController {

	private final SpotifyProxyService spotifyProxyService;

	@GetMapping("/api/getTestToken")
	public ResponseEntity<TestTokenData> getTestToken() {
		return spotifyProxyService.getTestToken();
	}
}
