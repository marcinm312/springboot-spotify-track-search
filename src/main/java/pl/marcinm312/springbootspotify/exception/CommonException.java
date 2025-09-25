package pl.marcinm312.springbootspotify.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CommonException extends RuntimeException {

	protected CommonException(String message) {
		super(message);
		log.error(getMessage());
	}

	protected CommonException(String message, Throwable cause) {
		super(message, cause);
		log.error(getMessage());
	}

	public abstract int getHttpStatus();
}
