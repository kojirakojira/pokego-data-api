package jp.brainjuice.pokego.business.service.utils.dto;

import jp.brainjuice.pokego.business.service.utils.ValidationService;
import jp.brainjuice.pokego.business.service.utils.ValidationService.CheckPattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * 入力チェックで使用するクラス
 *
 * @author saibabanagchampa
 * @see ValidationService
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationItem {
	private String itemName;
	private CheckPattern checkPattern;
	private Object constraint;
	private Object value;

	public ValidationItem setAll(String itemName, CheckPattern checkPattern, Object constraint, Object value) {
		setItemName(itemName);
		setCheckPattern(checkPattern);
		setConstraint(constraint);
		setValue(value);
		return this;
	}
}
