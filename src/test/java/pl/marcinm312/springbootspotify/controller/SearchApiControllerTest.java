package pl.marcinm312.springbootspotify.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import pl.marcinm312.springbootspotify.testdataprovider.ResponseReaderFromFile;
import pl.marcinm312.springbootspotify.testdataprovider.UserDataProvider;

import java.nio.file.FileSystems;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.SYSTEM_OUT, printOnlyOnFailure = false)
class SearchApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private MockRestServiceServer mockServer;

	@Autowired
	private RestTemplate restTemplate;

	private final ObjectMapper mapper = new ObjectMapper();

	private final OAuth2AuthenticatedPrincipal examplePrincipal = UserDataProvider.getOAuth2AuthenticatedPrincipal();

	@BeforeEach
	void setup() {
		mockServer = MockRestServiceServer.createServer(restTemplate);
	}

	@Test
	void search_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(
						get("/api/search?query=krzysztof krawczyk")
				)
				.andExpect(status().isUnauthorized());
	}

	@Test
	void search_simpleSearchCase_success() throws Exception {

		String spotifyUrl = "https://api.spotify.com/v1/search?q=krzysztof%2520krawczyk&type=track&market=PL&limit=50&offset=0";
		String filePath = "test_response" + FileSystems.getDefault().getSeparator() + "response.json";
		this.mockServer.expect(requestTo(spotifyUrl)).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(ResponseReaderFromFile.readResponseFromFile(filePath), MediaType.APPLICATION_JSON));

		String response = mockMvc.perform(
						get("/api/search?query=krzysztof krawczyk")
								.with(opaqueToken()
										.principal(examplePrincipal)
								))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		mockServer.verify();
		JsonNode root = mapper.readTree(response);
		Assertions.assertEquals(50, root.size());
	}

	@ParameterizedTest
	@MethodSource("examplesOfEmptySearch")
	void search_searchEmptyValue_emptyResult(String urlQuery) throws Exception {

		String response = mockMvc.perform(
						get("/api/search" + urlQuery)
								.with(opaqueToken()
										.principal(examplePrincipal)
								))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		mockServer.verify();
		JsonNode root = mapper.readTree(response);
		Assertions.assertEquals(0, root.size());
	}

	private static Stream<Arguments> examplesOfEmptySearch() {

		return Stream.of(
				Arguments.of(""),
				Arguments.of("?query="),
				Arguments.of("?query=        ")
		);
	}

	@Test
	void search_expiredSpotifySession_unauthorized() throws Exception {

		String spotifyUrl = "https://api.spotify.com/v1/search?q=krzysztof%2520krawczyk&type=track&market=PL&limit=50&offset=0";
		this.mockServer.expect(requestTo(spotifyUrl)).andExpect(method(HttpMethod.GET))
				.andRespond(withUnauthorizedRequest());

		mockMvc.perform(
						get("/api/search?query=krzysztof krawczyk")
								.with(opaqueToken()
										.principal(examplePrincipal)
								))
				.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@MethodSource("examplesOfSearchingWithIllegalCharacters")
	void search_illegalCharacters_errorMessage(String searchValue) throws Exception {

		String receivedErrorMessage = Objects.requireNonNull(mockMvc.perform(
						get("/api/search").param("query", searchValue)
								.with(opaqueToken()
										.principal(examplePrincipal)
								))
				.andExpect(status().isBadRequest())
				.andReturn().getResolvedException()).getMessage();

		String expectedErrorMessage = "Pole wyszukiwania zawiera niedozwolone znaki! Usuń je i spróbuj ponownie";
		Assertions.assertEquals(expectedErrorMessage, receivedErrorMessage);
	}

	private static Stream<Arguments> examplesOfSearchingWithIllegalCharacters() {

		return Stream.of(
				Arguments.of("Kombi!@#$%?"),
				Arguments.of("Kombi!@$%?"),
				Arguments.of("Krzysztof Krawczyk?#?#?#?#?#")
		);
	}

	@Test
	void getUserDetails_withAnonymousUser_unauthorized() throws Exception {

		mockMvc.perform(
						get("/api/me")
				)
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getUserDetails_simpleCase_success() throws Exception {

		String response = mockMvc.perform(
						get("/api/me")
								.with(opaqueToken()
										.principal(examplePrincipal)
								)
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		Map responseUserMap = mapper.readValue(response, Map.class);
		Assertions.assertEquals(UserDataProvider.getExampleUserParams().get("id"), responseUserMap.get("id"));
		Assertions.assertEquals(UserDataProvider.getExampleUserParams().get("display_name"), responseUserMap.get("display_name"));
		Assertions.assertEquals(UserDataProvider.getExampleUserParams().get("email"), responseUserMap.get("email"));
	}
}
