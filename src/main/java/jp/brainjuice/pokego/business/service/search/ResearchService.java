package jp.brainjuice.pokego.business.service.search;

import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;

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
