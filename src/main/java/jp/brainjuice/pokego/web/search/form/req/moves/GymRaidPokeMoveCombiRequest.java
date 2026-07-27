package jp.brainjuice.pokego.web.search.form.req.moves;

import jp.brainjuice.pokego.web.search.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class GymRaidPokeMoveCombiRequest extends ResearchRequestImpl {

	/** 返却値のmoveCombiListの件数の上限 */
	private long limit = -1L;
}
