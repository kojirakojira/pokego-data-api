package jp.brainjuice.pokego.business.service.utils.dto;

import jp.brainjuice.pokego.business.service.utils.InputCheckService.CheckPattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckItem {
	private String itemName;
	private CheckPattern checkPattern;
	private Object constraint;
	private Object value;

	public CheckItem setAll(String itemName, CheckPattern checkPattern, Object constraint, Object value) {
		setItemName(itemName);
		setCheckPattern(checkPattern);
		setConstraint(constraint);
		setValue(value);
		return this;
	}
}
