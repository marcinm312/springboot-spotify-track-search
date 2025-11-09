package pl.marcinm312.springbootspotify.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinm312.springbootspotify.model.dto.SpotifyTrackDto;
import pl.marcinm312.springbootspotify.service.SpotifyTracksClient;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/")
public class SearchApiController {

	private final SpotifyTracksClient spotifyTracksClient;

	@GetMapping("/search")
	public List<SpotifyTrackDto> search(BearerTokenAuthentication authentication, @RequestParam(required = false) String query) {

		String authenticationName = authentication.getName();
		log.info("user={}", authenticationName);
		String tokenValue = authentication.getToken().getTokenValue();
		return spotifyTracksClient.getTracksByAuthor(tokenValue, query);
	}

	@GetMapping("/me")
	public Map<String, Object> getUserDetails(BearerTokenAuthentication authentication) {
		return authentication.getTokenAttributes();
	}
}
