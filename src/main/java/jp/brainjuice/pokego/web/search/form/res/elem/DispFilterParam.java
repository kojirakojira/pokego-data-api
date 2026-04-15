package jp.brainjuice.pokego.web.search.form.res.elem;

import jp.brainjuice.pokego.dao.jpa.dto.FilterParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FilterParamを画面で表示する用の型
 *
 * @author saibabanagchampa
 * @see FilterParam
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispFilterParam {

	private String name;
	private String filterValue;
	private String negate = "";
}
