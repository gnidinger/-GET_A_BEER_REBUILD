package backend.domain.user.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import backend.domain.user.entity.User;

@Component
public class UserValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "korName", "BEER_NAME_IS_EMPTY", "Beer Name Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "country", "BEER_COUNTRY_IS_EMPTY", "Beer Country Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "abv", "BEER_ABV_IS_EMPTY", "Beer ABV Is Empty");

		User targetUser = (User)target;

		// if (!BEER_CATEGORY_SET.containsAll(targetRating.getBeerCategoryList())) {
		// 	errors.rejectValue("beerCategories", "BEER_CATEGORY_NOT_FOUND", "Beer Category Not Found");
		// }
		// if (targetRating.getBeerCategoryList().size() > 3) {
		// 	errors.rejectValue("beerCategories", "TOO_MANY_BEER_CATEGORIES", "Too Mant Beer Categories");
		// }

	}
}
