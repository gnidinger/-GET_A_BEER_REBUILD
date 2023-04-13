package backend.domain.user.validator;

import static backend.domain.constant.Constant.*;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import backend.domain.user.dto.UserRequestDto;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserUpdateInfoValidator implements Validator {


	@Override
	public boolean supports(Class<?> clazz) {
		return UserRequestDto.UpdateInfoRequest.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "nickname", "NICKNAME_IS_EMPTY", "Nickname Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "genderType", "GENDER_TYPE_IS_EMPTY", "Gender Type Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "ageType", "AGE_TYPE_IS_EMPTY", "Age Type Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "favoriteBeerCategoryList", "FAVORITE_BEER_CATEGORY_IS_EMPTY", "Favorite Beer Category Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "favoriteBeerTagList", "FAVORITE_BEER_TAG_IS_EMPTY", "Favorite Beer Tag Is Empty");

		UserRequestDto.UpdateInfoRequest targetUserDto = (UserRequestDto.UpdateInfoRequest)target;
		String nicknamePattern = "^[가-힣ㄱ-ㅎa-zA-Z0-9._-]{2,8}$";

		if (!Pattern.matches(nicknamePattern, targetUserDto.getNickname())) {
			errors.rejectValue("nickname", "NICKNAME_NOT_VALID", "닉네임은 2-8 자리의 숫자, 영어, 한국어와 언더스코어만 허용됩니다.");
		}
		if (!GENDER_TYPE_SET.contains(targetUserDto.getGenderType())) {
			errors.rejectValue("genderType", "GENDER_TYPE_NOT_VALID", "Gender Type Is Not Valid");
		}
		if (!AGE_TYPE_SET.contains(targetUserDto.getAgeType())) {
			errors.rejectValue("ageType", "AGE_TYPE_NOT_VALID", "Age Type Is Not Valid");
		}
		if (!BEER_CATEGORY_SET.containsAll(targetUserDto.getFavoriteBeerCategoryList())) {
			errors.rejectValue("favoriteBeerCategoryList", "FAVORITE_BEER_CATEGORY_NOT_VALID", "Favorite Beer Category Is Not Valid");
		}
		if (targetUserDto.getFavoriteBeerCategoryList().size() > 2) {
			errors.rejectValue("favoriteBeerCategoryList", "TOO_MANY_FAVORITE_BEER_CATEGORY", "선호 맥주 카테고리는 최대 두 개까지 선택할 수 있습니다.");
		}
		if (!BEER_TAG_SET.containsAll(targetUserDto.getFavoriteBeerTagList())) {
			errors.rejectValue("favoriteBeerTagList", "FAVORITE_BEER_TAG_NOT_VALID", "Favorite Beer Tag Is Not Valid");
		}
		if (targetUserDto.getFavoriteBeerTagList().size() > 4) {
			errors.rejectValue("favoriteBeerTagList", "TOO_MANY_FAVORITE_BEER_TAG", "선호 맥주 태그는 최대 네 개까지 선택할 수 있습니다.");
		}
	}
}

