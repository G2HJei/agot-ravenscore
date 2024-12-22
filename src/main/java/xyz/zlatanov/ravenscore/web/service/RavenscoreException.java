package xyz.zlatanov.ravenscore.web.service;

public class RavenscoreException extends RuntimeException {

	public RavenscoreException(String message) {
		super(message);
	}

	public RavenscoreException(String message, Throwable cause) {
		super(message, cause);
	}
}
