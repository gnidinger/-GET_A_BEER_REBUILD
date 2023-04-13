package backend.domain.pairing.validator;

import static backend.domain.constant.Constant.*;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import backend.domain.pairing.entity.Pairing;

@Component
public class PairingValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Pairing.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "content", "CONTENT_IS_EMPTY", "Content Is Empty");

		Pairing targetPairing = (Pairing)target;

		if (!PAIRING_CATEGORY_SET.contains(targetPairing.getPairingCategory())) {
			errors.rejectValue("pairingCategory", "PAIRING_CATEGORY_NOT_FOUND", "Pairing Category Not Found");
		}
	}
}
