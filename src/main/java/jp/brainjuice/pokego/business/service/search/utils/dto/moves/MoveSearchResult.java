package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import java.util.List;

import jp.brainjuice.pokego.dao.jpa.dto.SimpMove;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveSearchResult {

	private String message = "";

	private MsgLevelEnum msgLevel = MsgLevelEnum.info;

	/** 検索結果が1件かどうか */
	private boolean unique;

	/** 検索結果が複数件の場合 */
	private List<SimpMove> simpMoveList;

	/** 検索結果が1件の場合 */
	private SimpMove simpMove;

	/** あいまい検索によって検索された場合 */
	private boolean maybe;

	/** ヒットしたかどうか */
	private boolean hit;

	/** 検索したかどうか */
	private boolean searched;
}
