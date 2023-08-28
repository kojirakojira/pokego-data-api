package jp.brainjuice.pokego.web.form.res.scp;

import java.util.List;

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
public class ScpRankListResponse extends ResearchResponse {

	private List<ScpRank> scpRankList;
	private String league;
}
