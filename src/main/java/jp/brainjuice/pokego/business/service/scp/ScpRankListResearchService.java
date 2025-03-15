package jp.brainjuice.pokego.business.service.scp;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.ScpRankCalculator;
import jp.brainjuice.pokego.web.form.res.elem.ScpRank;
import jp.brainjuice.pokego.web.form.res.scp.ScpRankListResponse;

@Service
public class ScpRankListResearchService implements ResearchService<ScpRankListResponse> {

	private ScpRankCalculator scpRankCulculator;

	public ScpRankListResearchService(
			ScpRankCalculator scpRankCulculator) {
		this.scpRankCulculator = scpRankCulculator;
	}

	@Override
	public void exec(SearchValue sv, ScpRankListResponse res) {

		// leagueを取得
		String league = sv.getParamsMap().get(ParamsEnum.league, String.class);
		// scpRankListを生成
		List<ScpRank> scpRankList = scpRankCulculator.getSummary(
				sv.getGoPokedex(),
				league);

		// レスポンスのセット
		res.setScpRankList(scpRankList);
		res.setLeague(league);

		res.setMessage("");
	}

}
