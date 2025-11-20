package jp.brainjuice.pokego.web.search.form.req.moves;

import jp.brainjuice.pokego.web.search.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class GymRaidPokeMoveCombiRequest extends ResearchRequestImpl {

	/** シャドウか否か */
	private boolean shadow;
}
