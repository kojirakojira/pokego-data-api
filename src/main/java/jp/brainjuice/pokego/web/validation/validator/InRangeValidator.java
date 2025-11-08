package jp.brainjuice.pokego.web.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import jp.brainjuice.pokego.web.validation.InRange;

/**
 * jakarta.validationにint型のRangeをチェックするアノテーションが存在しないため実装
 *
 * @see InRange
 */
public class InRangeValidator implements ConstraintValidator<InRange, Number> {

	private int min;
	private int max;

	@Override
	public void initialize(InRange constraintAnnotation) {
		this.min = constraintAnnotation.min();
		this.max = constraintAnnotation.max();
	}

	@Override
	public boolean isValid(Number value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}

		int numericValue = value.intValue();
		return numericValue >= min && numericValue <= max;
	}

}
