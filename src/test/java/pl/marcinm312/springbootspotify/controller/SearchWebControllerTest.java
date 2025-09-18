package pl.marcinm312.springbootspotify.controller;

import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import pl.marcinm312.springbootspotify.model.dto.SpotifyAlbumDto;
import pl.marcinm312.springbootspotify.testdataprovider.ResponseReaderFromFile;
import pl.marcinm312.springbootspotify.testdataprovider.UserDataProvider;

import java.nio.file.FileSystems;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.SYSTEM_OUT, printOnlyOnFailure = false)
class SearchWebControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;

	private MockRestServiceServer mockServer;

	@Autowired
	private RestTemplate restTemplate;

	private final OAuth2User exampleOauth2User = UserDataProvider.getOAuth2User();

	@BeforeEach
	void setup() {
		mockServer = MockRestServiceServer.createServer(restTemplate);
	}

	@Test
	void search_withAnonymousUser_redirectToLoginPage() throws Exception {

		mockMvc.perform(
						get("/app/search/?query=krzysztof krawczyk")
				)
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/oauth2/authorization/spotify"))
				.andExpect(unauthenticated());
	}

	@Test
	void search_simpleSearchCase_success() throws Exception {

		String spotifyUrl = "https://api.spotify.com/v1/search?q=krzysztof%2520krawczyk&type=track&market=PL&limit=50&offset=0";
		String filePath = "test_response" + FileSystems.getDefault().getSeparator() + "response.json";
		this.mockServer.expect(requestTo(spotifyUrl)).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(ResponseReaderFromFile.readResponseFromFile(filePath), MediaType.APPLICATION_JSON));

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/search/?query=krzysztof krawczyk")
								.with(oauth2Login()
										.oauth2User(exampleOauth2User)
										.clientRegistration(clientRegistrationRepository.findByRegistrationId("spotify"))
								))
				.andExpect(status().isOk())
				.andExpect(view().name("search"))
				.andExpect(model().attribute("errorMessage", ""))
				.andExpect(model().attribute("userString", "Jan Kowalski (jan.kowalski@gmail.com)"))
				.andReturn().getModelAndView();

		mockServer.verify();
		assert modelAndView != null;
		List<SpotifyAlbumDto> albumListFromModel = (List<SpotifyAlbumDto>) modelAndView.getModel().get("searchResult");
		Assertions.assertEquals(50, albumListFromModel.size());
	}

	@ParameterizedTest
	@MethodSource("examplesOfEmptySearch")
	void search_searchEmptyValue_emptyResult(String urlQuery) throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/search/" + urlQuery)
								.with(oauth2Login()
										.oauth2User(exampleOauth2User)
										.clientRegistration(clientRegistrationRepository.findByRegistrationId("spotify"))
								))
				.andExpect(status().isOk())
				.andExpect(view().name("search"))
				.andExpect(model().attribute("errorMessage", ""))
				.andExpect(model().attribute("userString", "Jan Kowalski (jan.kowalski@gmail.com)"))
				.andReturn().getModelAndView();

		assert modelAndView != null;
		List<SpotifyAlbumDto> albumListFromModel = (List<SpotifyAlbumDto>) modelAndView.getModel().get("searchResult");
		Assertions.assertEquals(0, albumListFromModel.size());
	}

	private static Stream<Arguments> examplesOfEmptySearch() {

		return Stream.of(
				Arguments.of(""),
				Arguments.of("?query="),
				Arguments.of("?query=        ")
		);
	}

	@Test
	void search_expiredSpotifySession_unauthorizedMessage() throws Exception {

		String spotifyUrl = "https://api.spotify.com/v1/search?q=krzysztof%2520krawczyk&type=track&market=PL&limit=50&offset=0";
		this.mockServer.expect(requestTo(spotifyUrl)).andExpect(method(HttpMethod.GET))
				.andRespond(withUnauthorizedRequest());

		ModelAndView modelAndView = mockMvc.perform(
						get("/app/search/?query=krzysztof krawczyk")
								.with(oauth2Login()
										.oauth2User(exampleOauth2User)
										.clientRegistration(clientRegistrationRepository.findByRegistrationId("spotify"))
								))
				.andExpect(status().isOk())
				.andExpect(view().name("search"))
				.andExpect(model().attribute("errorMessage", StringStartsWith.startsWith("Błąd podczas wyszukiwania. Status HTTP: Unauthorized. Treść komunikatu: 401")))
				.andExpect(model().attribute("userString", "Jan Kowalski (jan.kowalski@gmail.com)"))
				.andReturn().getModelAndView();

		mockServer.verify();
		assert modelAndView != null;
		List<SpotifyAlbumDto> albumListFromModel = (List<SpotifyAlbumDto>) modelAndView.getModel().get("searchResult");
		Assertions.assertEquals(0, albumListFromModel.size());
	}

	@ParameterizedTest
	@MethodSource("examplesOfSearchingWithIllegalCharacters")
	void search_illegalCharacters_errorMessage(String searchValue) throws Exception {

		ModelAndView modelAndView = mockMvc.perform(
				get("/app/search/").param("query", searchValue)
						.with(oauth2Login()
								.oauth2User(exampleOauth2User)
								.clientRegistration(clientRegistrationRepository.findByRegistrationId("spotify"))
						))
				.andExpect(status().isOk())
				.andExpect(view().name("search"))
				.andExpect(model().attribute("errorMessage", StringStartsWith.startsWith("Błąd podczas wyszukiwania: Pole wyszukiwania zawiera niedozwolone znaki! Usuń je i spróbuj ponownie")))
				.andExpect(model().attribute("userString", "Jan Kowalski (jan.kowalski@gmail.com)"))
				.andReturn().getModelAndView();

		mockServer.verify();
		assert modelAndView != null;
		List<SpotifyAlbumDto> albumListFromModel = (List<SpotifyAlbumDto>) modelAndView.getModel().get("searchResult");
		Assertions.assertEquals(0, albumListFromModel.size());
	}

	private static Stream<Arguments> examplesOfSearchingWithIllegalCharacters() {

		return Stream.of(
				Arguments.of("Kombi!@#$%?"),
				Arguments.of("Kombi!@$%?"),
				Arguments.of("Krzysztof Krawczyk?#?#?#?#?#")
		);
	}
}
