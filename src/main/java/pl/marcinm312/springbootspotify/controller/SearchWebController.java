package pl.marcinm312.springbootspotify.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import pl.marcinm312.springbootspotify.exception.SpotifyException;
import pl.marcinm312.springbootspotify.exception.ValidationException;
import pl.marcinm312.springbootspotify.model.dto.SpotifyAlbumDto;
import pl.marcinm312.springbootspotify.service.SpotifyAlbumClient;
import pl.marcinm312.springbootspotify.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/app/")
public class SearchWebController {

	private final SpotifyAlbumClient spotifyAlbumClient;
	private final SessionUtils sessionUtils;
	private final OAuth2AuthorizedClientService authorizedClientService;

	private static final String SEARCH_ERROR_FORMAT = "Błąd podczas wyszukiwania: %s";

	@GetMapping("/search/")
	public String search(Model model, Authentication authentication, HttpServletResponse response,
						 @RequestParam(required = false) String query) {

		OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
		Map<String, Object> userDetails = authenticationToken.getPrincipal().getAttributes();
		String userString = userDetails.get("display_name") + " (" + userDetails.get("email") + ")";

		List<SpotifyAlbumDto> albumList = null;
		String errorMessage = "";

		try {
			String token = getJwtFromAuthorization(authenticationToken);
			albumList = spotifyAlbumClient.getAlbumsByAuthor(token, query);
		} catch (ResponseStatusException exc) {
			errorMessage = String.format("Błąd podczas wyszukiwania. Status HTTP: %s. Treść komunikatu: %s",
					exc.getStatusCode(), exc.getMessage());
			response.setStatus(exc.getStatusCode().value());
			if (HttpStatusCode.valueOf(401).equals(exc.getStatusCode())) {
				logoutAction();
			} else {
				errorMessage = errorMessage.replace("<EOL>", "");
			}
		} catch (ValidationException | SpotifyException exc) {
			errorMessage = String.format(SEARCH_ERROR_FORMAT, exc.getMessage());
			response.setStatus(exc.getHttpStatus());
		} catch (NullPointerException exc) {
			errorMessage = String.format(SEARCH_ERROR_FORMAT, exc.getMessage());
			log.error(errorMessage, exc);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logoutAction();
		} catch (Exception exc) {
			errorMessage = String.format(SEARCH_ERROR_FORMAT, exc.getMessage());
			log.error(errorMessage, exc);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		model.addAttribute("searchResult", albumList != null ? albumList : new ArrayList<>());
		model.addAttribute("errorMessage", errorMessage);
		model.addAttribute("userString", userString);
		model.addAttribute("query", query);

		return "search";
	}

	private void logoutAction() {
		sessionUtils.expireCurrentSession();
	}

	private String getJwtFromAuthorization(OAuth2AuthenticationToken authenticationToken) {

		if (authenticationToken == null) {
			return null;
		}
		String authenticationName = authenticationToken.getName();
		log.info("user={}", authenticationName);
		OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient
				(authenticationToken.getAuthorizedClientRegistrationId(), authenticationName);
		if (client == null) {
			return null;
		}
		return client.getAccessToken().getTokenValue();
	}
}
