package jp.brainjuice.pokego.business.service.search.pokeFilter;

import java.util.List;

import jp.brainjuice.pokego.business.service.search.utils.dto.GppAndCp;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.elem.DispFilterParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokemonFilterResult {

	private String message = "";

	private MsgLevelEnum msgLevel = MsgLevelEnum.info;

	/** 検索結果が1件かどうか */
	private boolean unique;

	/** 検索結果が複数件の場合 */
	private List<GppAndCp> gpAndCpList;

	/** 検索結果が1件の場合 */
	private GppAndCp goPokedex;

	/** ヒットしたかどうか */
	private boolean hit;

	/** 絞り込みをした検索値 */
	private List<DispFilterParam> filteredItems;
}
