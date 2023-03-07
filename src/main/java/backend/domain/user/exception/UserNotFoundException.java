package backend.domain.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
	value = HttpStatus.NOT_FOUND,
	reason = "User Not Found")
public class UserNotFoundException extends RuntimeException {

	private static final String message = "User Not Found";

	public UserNotFoundException() {
		super(message);
	}
}
