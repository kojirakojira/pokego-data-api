package jp.brainjuice.pokego.business.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 絞り込み用のパラメータ
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterParam {

	private Object filterValue;
	private boolean negate;

	/**
	 * 否定形の検索モジュールを設けていない場合に使用するコンストラクタ
	 * @param filterValue
	 */
	public FilterParam(Object filterValue) {
		setFilterValue(filterValue);
	}
}
