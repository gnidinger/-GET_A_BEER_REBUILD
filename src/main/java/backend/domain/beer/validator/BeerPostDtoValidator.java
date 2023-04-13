package backend.domain.beer.validator;

import static backend.domain.constant.Constant.*;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import backend.domain.beer.dto.BeerRequestDto;

@Component
public class BeerPostDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return BeerRequestDto.Post.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "korName", "BEER_NAME_IS_EMPTY", "Beer Name Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "country", "BEER_COUNTRY_IS_EMPTY", "Beer Country Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "abv", "BEER_ABV_IS_EMPTY", "Beer ABV Is Empty");

		BeerRequestDto.Post targetBeerDto = (BeerRequestDto.Post)target;

		if (!BEER_CATEGORY_SET.containsAll(targetBeerDto.getBeerCategories())) {
			errors.rejectValue("beerCategories", "BEER_CATEGORY_NOT_FOUND", "Beer Category Not Found");
		}
		if (targetBeerDto.getBeerCategories().size() > 3) {
			errors.rejectValue("beerCategories", "TOO_MANY_BEER_CATEGORIES", "Too Mant Beer Categories");
		}
	}
}
