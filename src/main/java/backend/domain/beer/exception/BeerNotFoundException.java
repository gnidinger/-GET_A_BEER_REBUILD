package backend.domain.beer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
	value = HttpStatus.NOT_FOUND,
	reason = "Beer Not Found")
// @ExceptionHandler(BeerNotFoundException.class)
public class BeerNotFoundException extends RuntimeException {

	private static final String message = "Beer Not Found";

	public BeerNotFoundException() {
		super(message);
	}
}
