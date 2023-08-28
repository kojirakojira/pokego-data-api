package jp.brainjuice.pokego.web.form.req.catchCp;

import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RaidRequest extends ResearchRequestImpl {

	/** シャドウか否か */
	private boolean shadow;
}
