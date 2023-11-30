package jp.brainjuice.pokego.web.form.res.catchCp;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class FRTaskResponse extends ResearchResponse {

	private boolean mega = false;
	private GoPokedex befMegaGp;

	private int maxCp;
	private int minCp;
}