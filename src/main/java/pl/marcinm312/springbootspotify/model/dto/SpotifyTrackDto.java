package pl.marcinm312.springbootspotify.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
public final class SpotifyTrackDto {

	private final String trackName;
	private final List<String> artists;
	private final String albumName;
	private final String imageUrl;
	private final String audioPreviewUrl;

	@JsonIgnore
	public String getArtistsAsString() {
		return String.join(", ", artists);
	}
}
