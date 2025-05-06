package jp.brainjuice.pokego.web.search.form.res.scp;

import java.util.List;

import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.ScpRankAllInOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class ScpRankResponse extends ResearchResponse {

	private ScpRankAllInOne targetScpRank;
	
	private List<ScpRankAllInOne> afEvolScpRankList;
	
	private List<ScpRankAllInOne> anotherFormScpRankList;
}
