package backend.domain.beer.validator;

import static backend.domain.constant.Constant.*;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import backend.domain.beer.entity.Beer;

@Component
public class BeerValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Beer.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "korName", "BEER_NAME_IS_EMPTY", "Beer Name Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "country", "BEER_COUNTRY_IS_EMPTY", "Beer Country Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "abv", "BEER_ABV_IS_EMPTY", "Beer ABV Is Empty");

		Beer targetBeer = (Beer)target;

		if (!BEER_CATEGORY_SET.containsAll(targetBeer.getBeerCategoryList())) {
			errors.rejectValue("beerCategories", "BEER_CATEGORY_NOT_FOUND", "Beer Category Not Found");
		}
		if (targetBeer.getBeerCategoryList().size() > 3) {
			errors.rejectValue("beerCategories", "TOO_MANY_BEER_CATEGORIES", "Too Mant Beer Categories");
		}

	}
}
