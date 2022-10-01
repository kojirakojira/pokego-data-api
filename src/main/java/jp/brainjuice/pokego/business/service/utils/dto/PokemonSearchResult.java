package jp.brainjuice.pokego.business.service.utils.dto;

import java.util.List;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokemonSearchResult {

	private String message = "";

	/** 検索結果が1件かどうか */
	private boolean unique;

	/** 検索結果が複数件の場合 */
	private List<GoPokedex> goPokedexList;

	/** 検索結果が1件の場合 */
	private GoPokedex goPokedex;

	/** あいまい検索によって検索された場合 */
	private boolean maybe;
}
