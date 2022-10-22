package jp.brainjuice.pokego.business.service.utils.dto;

import jp.brainjuice.pokego.business.service.utils.InputCheckService.CheckPattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInfo {
	private String itemName;
	private CheckPattern checkPattern;
	private Object constraint;
	private Object value;
}
