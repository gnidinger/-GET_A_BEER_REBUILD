package backend.domain.rating.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
	value = HttpStatus.UNAUTHORIZED,
	reason = "User Not Match")
public class UserNotMatchException extends RuntimeException {

	private static final String message = "User Not Match";

	public UserNotMatchException() {
		super(message);
	}
}
