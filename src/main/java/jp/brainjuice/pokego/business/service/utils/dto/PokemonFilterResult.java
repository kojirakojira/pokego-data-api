package jp.brainjuice.pokego.business.service.utils.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokemonFilterResult {

	private String message = "";

	/** 検索結果が1件かどうか */
	private boolean unique;

	/** 検索結果が複数件の場合 */
	private List<GoPokedexPlusAlpha> goPokedexList;

	/** 検索結果が1件の場合 */
	private GoPokedexPlusAlpha goPokedex;

	/** ヒットしたかどうか */
	private boolean hit;

	/** 絞り込みをした検索値 */
	private List<Map<String, String>> filteredItems;
}
