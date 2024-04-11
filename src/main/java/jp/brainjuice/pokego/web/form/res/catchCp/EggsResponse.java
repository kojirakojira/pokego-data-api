package jp.brainjuice.pokego.web.form.res.catchCp;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.form.res.elem.CatchCp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class EggsResponse extends ResearchResponse {

	/** 進化前で算出したか、そのまま算出したか */
	private boolean before = false;
	private GoPokedex befGp;

	private CatchCp catchCp;
}
