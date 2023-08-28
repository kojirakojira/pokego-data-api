package jp.brainjuice.pokego.web.form.res;

import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ResearchSearviceExecutorを使用してポケモン情報の検索をする場合に継承するインタフェースです。
 *
 * @author saibabanagchampa
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class ResearchResponse extends Response {

	private PokemonSearchResult pokemonSearchResult;
	private String pokedexId;
	private String name;
	private String remarks;
}
