package pl.marcinm312.springbootspotify.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import pl.marcinm312.springbootspotify.testdataprovider.ResponseReaderFromFile;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.SYSTEM_OUT, printOnlyOnFailure = false)
class SearchApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private MockRestServiceServer mockServer;

	@Autowired
	private RestTemplate restTemplate;

	@BeforeEach
	void setup() {
		mockServer = MockRestServiceServer.createServer(restTemplate);
	}

	@Test
	void search() throws Exception {

		String spotifyUrl = "https://api.spotify.com/v1/search?q=krzysztof%2520krawczyk&type=track&market=PL&limit=50&offset=0";
		String filePath = "test_response" + FileSystems.getDefault().getSeparator() + "response.json";
		this.mockServer.expect(requestTo(spotifyUrl)).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(ResponseReaderFromFile.readResponseFromFile(filePath), MediaType.APPLICATION_JSON));

		Map<String, Object> userParams = new HashMap<>();
		userParams.put("user_name", "foo_user");
		userParams.put("display_name", "Jan Kowalski");
		userParams.put("email", "jan.kowalski@gmail.com");

		OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(
				(String) userParams.get("user_name"),
				userParams,
				AuthorityUtils.createAuthorityList("SCOPE_message:read"));

		mockMvc.perform(
				get("/api/search?query=krzysztof krawczyk").with(opaqueToken()
						.principal(principal)
				)
		).andExpect(status().isOk());

		mockServer.verify();
	}

	@Test
	void search401() throws Exception {

		mockMvc.perform(
				get("/api/search?query=krzysztof krawczyk")
		).andExpect(status().isUnauthorized());
	}

	@Test
	void getUserDetails() throws Exception {

		Map<String, Object> userParams = new HashMap<>();
		userParams.put("user_name", "foo_user");
		userParams.put("display_name", "Jan Kowalski");
		userParams.put("email", "jan.kowalski@gmail.com");

		OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(
				(String) userParams.get("user_name"),
				userParams,
				AuthorityUtils.createAuthorityList("SCOPE_message:read"));

		mockMvc.perform(
				get("/api/me").with(opaqueToken()
						.principal(principal)
				)
		).andExpect(status().isOk());
	}

	@Test
	void getUserDetails401() throws Exception {

		mockMvc.perform(
				get("/api/me")
		).andExpect(status().isUnauthorized());
	}
}
