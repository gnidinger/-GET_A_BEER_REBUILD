package backend.global.exception;

import lombok.Getter;

public enum ExceptionCode {

	BEER_NAME_EMPTY(400, "Beer Name Is Empty"),
	BEER_COUNTRY_EMPTY(400, "Beer Country Is Empty"),
	BEER_ABV_EMPTY(400, "Beer ABV Is Empty"),
	BEER_NOT_FOUND(404, "Beer Not Found"),

	BEER_CATEGORY_NOT_FOUND(404, "Beer Category Not Found");

	@Getter
	private int status;
	@Getter
	private String message;

	ExceptionCode(int status, String message) {
		this.status = status;
		this.message = message;
	}
}
