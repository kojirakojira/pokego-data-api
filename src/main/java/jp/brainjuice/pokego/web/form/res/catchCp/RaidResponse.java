package jp.brainjuice.pokego.web.form.res.catchCp;

import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class RaidResponse extends ResearchResponse {

	private boolean shadow;

	private int maxCp;
	private int minCp;

	/** 天候ブースト時 */
	private int wbMaxCp;
	private int wbMinCp;
}
