package pl.marcinm312.springbootspotify.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SpotifyException extends CommonException {

	public SpotifyException(String message, Throwable cause) {
		super(message, cause);
	}

	public int getHttpStatus() {
		return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}
}
