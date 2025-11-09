package pl.marcinm312.springbootspotify.model.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.marcinm312.springbootspotify.model.Artist;
import pl.marcinm312.springbootspotify.model.Item;
import pl.marcinm312.springbootspotify.model.dto.SpotifyTrackDto;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpotifyTrackMapper {

	public static SpotifyTrackDto convertSpotifyItemToDto(Item item) {

		List<String> artists = item.getArtists().stream()
				.map(Artist::getName)
				.filter(Objects::nonNull)
				.toList();

		return SpotifyTrackDto.builder()
				.trackName(item.getName())
				.imageUrl(item.getAlbum().getImages().getFirst().getUrl())
				.audioPreviewUrl(item.getPreviewUrl())
				.artists(artists)
				.albumName(item.getAlbum().getName())
				.build();
	}

	public static List<SpotifyTrackDto> convertSpotifyItemsToDtoList(List<Item> items) {

		return items.stream()
				.map(SpotifyTrackMapper::convertSpotifyItemToDto)
				.toList();
	}
}
