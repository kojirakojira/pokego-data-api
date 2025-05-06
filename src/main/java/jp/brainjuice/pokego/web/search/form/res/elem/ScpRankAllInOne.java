package jp.brainjuice.pokego.web.search.form.res.elem;

import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ScpRankのオールインワン！！！
 */
@Data
@AllArgsConstructor
public class ScpRankAllInOne {

	private GoPokedex goPokedex;
	/** スーパーリーグ */
	private ScpRank sl;
	/** ハイパーリーグ */
	private ScpRank hl;
	/** マスターリーグ */
	private ScpRank ml;
}
