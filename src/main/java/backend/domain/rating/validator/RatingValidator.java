package backend.domain.rating.validator;

import static backend.domain.constant.Constant.*;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import backend.domain.beer.repository.BeerRepository;
import backend.domain.beer.service.BeerService;
import backend.domain.rating.entity.Rating;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RatingValidator implements Validator {
	private final BeerService beerService;
	private final BeerRepository beerRepository;

	@Override
	public boolean supports(Class<?> clazz) {
		return Rating.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "star", "STAR_IS_EMPTY", "Star Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "content", "CONTENT_IS_EMPTY", "Content Is Empty");

		Rating targetRating = (Rating)target;

		if (targetRating.getStar() == 0) {
			errors.rejectValue("star", "STAR_IS_ZERO", "Star is Zero");
		}

		if (targetRating.getBeerTagList().size() > 4) {
			errors.rejectValue("beerTagList", "TOO_MANY_TAG_CATEGORIES", "Too Many Beer Tags");
		}

		if (!BEER_TAG_SET.containsAll(targetRating.getBeerTagList())) {
			errors.rejectValue("beerTagList", "BEER_TAG_NOT_FOUND", "Beer Tag Not Found");
		}
	}

	public void validate(Object target, String beerId, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "star", "STAR_IS_EMPTY", "Star Is Empty");
		ValidationUtils.rejectIfEmpty(errors, "content", "CONTENT_IS_EMPTY", "Content Is Empty");

		Rating targetRating = (Rating)target;

		if (targetRating.getStar() == 0) {
			errors.rejectValue("star", "STAR_IS_ZERO", "Star is Zero");
		}

		if (targetRating.getBeerTagList().size() > 4) {
			errors.rejectValue("beerTagList", "TOO_MANY_Tag_CATEGORIES", "Too Many Beer Tags");
		}

		if (!BEER_TAG_SET.containsAll(targetRating.getBeerTagList())) {
			errors.rejectValue("beerTagList", "BEER_TAG_NOT_FOUND", "Beer Tag Not Found");
		}
	}
}
