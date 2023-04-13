package backend.domain.rating.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
	value = HttpStatus.NOT_FOUND,
	reason = "Rating Not Found")
public class RatingNotFoundException extends RuntimeException {

	private static final String message = "Rating Not Found";

	public RatingNotFoundException() {
		super(message);
	}
}
