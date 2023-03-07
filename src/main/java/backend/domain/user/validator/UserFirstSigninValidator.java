package backend.domain.user.validator;


import static backend.domain.constant.Constant.*;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import backend.domain.user.dto.UserRequestDto;

@Component
public class UserFirstSigninValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return UserRequestDto.FirstSignIn.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "genderType", "GENDER_TYPE_IS_EMPTY", "Gender Type Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "ageType", "AGE_TYPE_IS_EMPTY", "Age Type Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "favoriteBeerCategoryList", "FAVORITE_BEER_CATEGORY_IS_EMPTY", "Favorite Beer Category Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "favoriteBeerTagList", "FAVORITE_BEER_TAG_IS_EMPTY", "Favorite Beer Tag Is Empty");

		UserRequestDto.FirstSignIn targetUserDto = (UserRequestDto.FirstSignIn)target;

		if (!GENDER_TYPE_SET.contains(targetUserDto.getGenderType())) {
			errors.rejectValue("genderType", "GENDER_TYPE_NOT_VALID", "Gender Type Is Not Valid");
		}
		if (!AGE_TYPE_SET.contains(targetUserDto.getAgeType())) {
			errors.rejectValue("ageType", "AGE_TYPE_NOT_VALID", "Age Type Is Not Valid");
		}
		if (!BEER_CATEGORY_SET.containsAll(targetUserDto.getFavoriteBeerCategoryList())) {
			errors.rejectValue("favoriteBeerCategoryList", "FAVORITE_BEER_CATEGORY_NOT_VALID", "Favorite Beer Category Is Not Valid");
		}
		if (!BEER_TAG_SET.containsAll(targetUserDto.getFavoriteBeerTagList())) {
			errors.rejectValue("favoriteBeerTagList", "FAVORITE_BEER_TAG_NOT_VALID", "Favorite Beer Tag Is Not Valid");
		}
	}
}
