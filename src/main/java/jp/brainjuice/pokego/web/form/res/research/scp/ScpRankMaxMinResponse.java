package jp.brainjuice.pokego.web.form.res.research.scp;

import jp.brainjuice.pokego.web.form.res.elem.ScpRank;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class ScpRankMaxMinResponse extends ResearchResponse {

	/** スーパーリーグpvp順位：最高 */
	private ScpRank scpSlRankMax;
	/** スーパーリーグpvp順位：最低 */
	private ScpRank scpSlRankMin;
	/** ハイパーリーグpvp順位：最高 */
	private ScpRank scpHlRankMax;
	/** ハイパーリーグpvp順位：最低 */
	private ScpRank scpHlRankMin;
	/** マスターリーグpvp順位：最高 */
	private ScpRank scpMlRankMax;
	/** マスターリーグpvp順位：最低 */
	private ScpRank scpMlRankMin;
}
