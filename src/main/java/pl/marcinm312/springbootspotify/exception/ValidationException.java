package pl.marcinm312.springbootspotify.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends CommonException {

	public ValidationException(String message) {
		super(message);
	}

	public int getHttpStatus() {
		return HttpServletResponse.SC_BAD_REQUEST;
	}
}
