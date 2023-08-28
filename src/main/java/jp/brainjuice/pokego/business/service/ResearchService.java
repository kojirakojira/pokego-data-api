package jp.brainjuice.pokego.business.service;

import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;

/**
 * ポケモン1体の個体値、CP等を調べるときに使用するインターフェースです。
 *
 * @author saibabanagchampa
 *
 * @param <T>
 */
public interface ResearchService<T extends ResearchResponse> {

	/**
	 * 実行します。
	 *
	 * @param SearchValue sv
	 * @param T(extends Response) res
	 */
	public void exec(SearchValue sv, T res);

}
