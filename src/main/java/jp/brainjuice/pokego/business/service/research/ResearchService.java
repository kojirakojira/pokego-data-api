package jp.brainjuice.pokego.business.service.research;

import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.web.form.res.Response;

/**
 * ポケモン1体の個体値、CP等を調べるときに使用するインターフェースです。
 *
 * @author saibabanagchampa
 *
 * @param <T>
 */
public interface ResearchService<T extends Response> {

	/**
	 * 実行します。
	 *
	 * @param IndividialValue iv
	 * @param T(extends Response) res
	 */
	public void exec(IndividialValue iv, T res);

}
