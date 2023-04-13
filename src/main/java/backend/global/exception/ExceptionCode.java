package backend.global.exception;

import lombok.Getter;

public enum ExceptionCode {

	UNAUTHORIZED(401, "Unauthorized"),
	SIGNIN_FAILED(401, "Signin Failed"),
	TOKEN_EXPIRED(401, "Token Expired"),
	TOKEN_NOT_FOUND(400, "Token Not Found"),
	TOKEN_NOT_MATCHED(401, "Tokens Are Not Matched"),
	EMAIL_EXIST(400, "Email Exist"),
	EMAIL_USED_ANOTHER_ACCOUNT(404, "Email is Being Used By Another Account."),
	NICKNAME_EXIST(400, "Nickname Exist"),
	USER_NOT_FOUND(404, "User Not Found"),
	BEER_NAME_EMPTY(400, "Beer Name Is Empty"),
	BEER_COUNTRY_EMPTY(400, "Beer Country Is Empty"),
	BEER_ABV_EMPTY(400, "Beer ABV Is Empty"),
	BEER_NOT_FOUND(404, "Beer Not Found"),
	BEER_MISMATCH(404, "Beer Mismatch"),

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
