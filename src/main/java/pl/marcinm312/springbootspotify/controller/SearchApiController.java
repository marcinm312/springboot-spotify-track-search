package pl.marcinm312.springbootspotify.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinm312.springbootspotify.model.dto.SpotifyAlbumDto;
import pl.marcinm312.springbootspotify.service.SpotifyAlbumClient;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/")
public class SearchApiController {

	private final SpotifyAlbumClient spotifyAlbumClient;

	@GetMapping("/search/")
	public List<SpotifyAlbumDto> search(Authentication authentication, @RequestParam(required = false) String query) {

		OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
		return spotifyAlbumClient.getAlbumsByAuthor(authenticationToken, query);
	}
}
