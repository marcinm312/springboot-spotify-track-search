package pl.marcinm312.springbootspotify.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import pl.marcinm312.springbootspotify.exception.SpotifyException;
import pl.marcinm312.springbootspotify.exception.ValidationException;
import pl.marcinm312.springbootspotify.model.Artist;
import pl.marcinm312.springbootspotify.model.Item;
import pl.marcinm312.springbootspotify.model.SpotifyAlbum;
import pl.marcinm312.springbootspotify.model.dto.SpotifyAlbumDto;
import pl.marcinm312.springbootspotify.utils.StringMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Service
public class SpotifyAlbumClient {

	private final RestTemplate restTemplate;

	public List<SpotifyAlbumDto> getAlbumsByAuthor(String token, String authorName) {

		if (StringUtils.isBlank(authorName)) {
			return new ArrayList<>();
		}

		ResponseEntity<SpotifyAlbum> exchange = getSpotifyAlbumResponseEntity(authorName, token);
		HttpStatusCode httpStatus = exchange.getStatusCode();
		log.info("status={}", httpStatus);
		if (!httpStatus.is2xxSuccessful()) {
			throw new ResponseStatusException(httpStatus);
		}

		SpotifyAlbum spotifyAlbum = exchange.getBody();
		if (spotifyAlbum == null) {
			return new ArrayList<>();
		}

		List<SpotifyAlbumDto> albumList = spotifyAlbum.getTracks().getItems()
				.stream()
				.map(SpotifyAlbumClient::convertSpotifyItemToDto)
				.toList();
		log.info("albumList.size()={}", albumList.size());
		return albumList;
	}

	private static SpotifyAlbumDto convertSpotifyItemToDto(Item item) {

		List<String> artists = item.getArtists().stream()
				.map(Artist::getName)
				.filter(Objects::nonNull)
				.toList();

		return SpotifyAlbumDto.builder()
				.trackName(item.getName())
				.imageUrl(item.getAlbum().getImages().getFirst().getUrl())
				.audioPreviewUrl(item.getPreviewUrl())
				.artists(artists)
				.albumName(item.getAlbum().getName())
				.build();
	}

	private ResponseEntity<SpotifyAlbum> getSpotifyAlbumResponseEntity(String authorName, String jwt) {

		HttpHeaders httpHeaders = new HttpHeaders();
		if (jwt != null) {
			httpHeaders.add("Authorization", "Bearer " + jwt);
		}
		HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

		String url = "https://api.spotify.com/v1/search?q=" + authorName.trim().replace(" ", "%20")
				+ "&type=track&market=PL&limit=50&offset=0";
		log.info("url={}", url);
		if (!StringMethods.isValidUrl(url)) {
			throw new ValidationException("Pole wyszukiwania zawiera niedozwolone znaki! Usuń je i spróbuj ponownie");
		}

		try {
			return restTemplate.exchange(
					url,
					HttpMethod.GET,
					httpEntity,
					SpotifyAlbum.class);
		} catch (HttpClientErrorException e) {
			log.error(e.getMessage(), e);
			throw new ResponseStatusException(e.getStatusCode(), e.getMessage(), e);
		} catch (Exception e) {
			throw new SpotifyException(e.getMessage(), e);
		}
	}
}
