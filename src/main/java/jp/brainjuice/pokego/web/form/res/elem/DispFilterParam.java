package jp.brainjuice.pokego.web.form.res.elem;

import jp.brainjuice.pokego.business.dao.dto.FilterParam;
import lombok.Data;

/**
 * FilterParamを画面で表示する用の型
 *
 * @author saibabanagchampa
 * @see FilterParam
 *
 */
@Data
public class DispFilterParam {

	private String name;
	private String filterValue;
	private String negate = "";
}
