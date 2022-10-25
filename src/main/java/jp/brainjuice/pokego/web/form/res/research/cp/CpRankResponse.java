package jp.brainjuice.pokego.web.form.res.research.cp;

import jp.brainjuice.pokego.web.form.res.elem.CpRank;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class CpRankResponse extends ResearchResponse {

	private CpRank cpRank;
}
