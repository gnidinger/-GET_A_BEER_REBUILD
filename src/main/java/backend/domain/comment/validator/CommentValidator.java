package backend.domain.comment.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import backend.domain.comment.entity.Comment;

@Component
public class CommentValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Comment.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "content", "CONTENT_IS_EMPTY", "Content Is Empty");

		Comment targetComment = (Comment)target;

	}
}
