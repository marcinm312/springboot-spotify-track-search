package pl.marcinm312.springbootspotify.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.SpyBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import pl.marcinm312.springbootspotify.config.BeansConfig;
import pl.marcinm312.springbootspotify.config.WebSecurityConfig;
import pl.marcinm312.springbootspotify.service.SpotifyAlbumClient;
import pl.marcinm312.springbootspotify.testdataprovider.ResponseReaderFromFile;
import pl.marcinm312.springbootspotify.utils.SessionUtils;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@ComponentScan(basePackageClasses = SearchWebController.class,
		useDefaultFilters = false,
		includeFilters = {
				@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = SearchWebController.class)
		})
@Import({WebSecurityConfig.class})
@MockBeans({@MockBean(SessionUtils.class)})
@SpyBeans({@SpyBean(OAuth2AuthorizedClientService.class)})
@ContextConfiguration(classes = BeansConfig.class)
@WebAppConfiguration
class SearchWebControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;

	private MockRestServiceServer mockServer;

	@Autowired
	private RestTemplate restTemplate;

	@SpyBean
	private SpotifyAlbumClient spotifyAlbumClient;

	@BeforeEach
	void setup() {

		mockServer = MockRestServiceServer.createServer(restTemplate);
		mockMvc = MockMvcBuilders
				.webAppContextSetup(this.webApplicationContext)
				.apply(springSecurity())
				.alwaysDo(print())
				.build();
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
		OAuth2User oauth2User = new DefaultOAuth2User(
				AuthorityUtils.createAuthorityList("SCOPE_message:read"),
				userParams, "user_name");
		
		mockMvc.perform(
				get("/app/search/?query=krzysztof krawczyk")
						.with(oauth2Login()
								.oauth2User(oauth2User)
								.clientRegistration(clientRegistrationRepository.findByRegistrationId("spotify"))
						)
		);

		mockServer.verify();
		verify(spotifyAlbumClient, times(1)).getAlbumsByAuthor(any(), eq("krzysztof krawczyk"));
	}
}