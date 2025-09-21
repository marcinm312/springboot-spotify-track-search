package pl.marcinm312.springbootspotify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SpotifyException extends RuntimeException {

	public SpotifyException(String message, Throwable cause) {
		super(message, cause);
	}
}
