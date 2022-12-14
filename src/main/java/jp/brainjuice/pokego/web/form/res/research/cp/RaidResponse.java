package jp.brainjuice.pokego.web.form.res.research.cp;

import jp.brainjuice.pokego.web.form.res.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class RaidResponse extends Response {

	private int maxCp;
	private int minCp;

	/** 天候ブースト時 */
	private int wbMaxCp;
	private int wbMinCp;
}
