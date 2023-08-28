package jp.brainjuice.pokego.web.form.res.research.scp;

import java.util.List;

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
public class ScpRankListResponse extends ResearchResponse {

	private List<ScpRank> scpRankList;
	private String league;
}
