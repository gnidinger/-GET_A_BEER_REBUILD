package backend.domain.user.validator;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import backend.domain.user.dto.UserRequestDto;

@Component
public class UserSignupValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return UserRequestDto.SignUp.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "email", "EMAIL_IS_EMPTY", "Email Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "nickname", "NICKNAME_IS_EMPTY", "Nickname Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "password", "PASSWORD_IS_EMPTY", "Password Is Empty");

		String emailPattern = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";
		String passwordPattern = "^(?=.*?\\d{1,50})(?=.*?[~`!@#$%^&()-+=]{1,50})(?=.*?[a-zA-Z]{2,50}).{8,16}$";
		String nicknamePattern = "^[가-힣ㄱ-ㅎa-zA-Z0-9._-]{2,8}$";

		UserRequestDto.SignUp targetUserDto = (UserRequestDto.SignUp)target;

		if (!Pattern.matches(emailPattern, targetUserDto.getEmail())) {
			errors.rejectValue("email", "EMAIL_NOT_VALID", "Email Is Not Valid");
		}
		if (!Pattern.matches(passwordPattern, targetUserDto.getPassword())) {
			errors.rejectValue("password", "PASSWORD_NOT_VALID", "비밀번호는 8자 이상 특수문자와 영어 대소문자, 숫자만 허용됩니다.");
		}
		if (!Pattern.matches(nicknamePattern, targetUserDto.getNickname())) {
			errors.rejectValue("nickname", "NICKNAME_NOT_VALID", "닉네임은 숫자, 영어, 한국어와 언더스코어만 허용됩니다.");
		}
	}
}
