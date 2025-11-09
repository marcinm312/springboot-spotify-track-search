package pl.marcinm312.springbootspotify.model.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class SpotifyTrackDtoTest {

	@Test
	void equalsHashCode_differentCases() {
		EqualsVerifier.forClass(SpotifyTrackDto.class).verify();
	}
}