package pl.marcinm312.springbootspotify.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	public List<SpotifyAlbumDto> search(HttpServletRequest request, @RequestParam(required = false) String query) {

		String tokenValue = request.getHeader("Authorization");
		if (tokenValue != null && tokenValue.startsWith("Bearer ")) {
			tokenValue = tokenValue.substring(7);
		}
		return spotifyAlbumClient.getAlbumsByAuthor(tokenValue, query);
	}
}
