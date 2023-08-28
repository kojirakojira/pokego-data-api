package jp.brainjuice.pokego.web.form.res.scp;

import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.form.res.elem.ScpRank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class ScpRankResponse extends ResearchResponse {

	/** スーパーリーグpvp順位 */
	private ScpRank scpSlRank;
	/** ハイパーリーグpvp順位 */
	private ScpRank scpHlRank;
	/** マスターリーグpvp順位 */
	private ScpRank scpMlRank;
}
