package backend.domain.pairing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
	value = HttpStatus.NOT_FOUND,
	reason = "Pairing Not Found")
public class PairingNotFoundException extends RuntimeException {

	private static final String message = "Pairing Not Found";

	public PairingNotFoundException() {
		super(message);
	}
}
