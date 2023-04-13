package backend.domain.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
	value = HttpStatus.NOT_FOUND,
	reason = "Room Not Found")
public class RoomNotFoundException extends RuntimeException {

	private static final String message = "Room Not Found";

	public RoomNotFoundException() {
		super(message);
	}
}
