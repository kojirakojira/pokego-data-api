package jp.brainjuice.pokego.business.service.scp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.utils.ScpRankCalculator;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.web.form.res.elem.ScpRank;
import jp.brainjuice.pokego.web.form.res.scp.ScpRankListResponse;

@Service
public class ScpRankListResearchService implements ResearchService<ScpRankListResponse> {

	private ScpRankCalculator scpRankCulculator;

	@Autowired
	public ScpRankListResearchService(
			ScpRankCalculator scpRankCulculator) {
		this.scpRankCulculator = scpRankCulculator;
	}

	@Override
	public void exec(SearchValue sv, ScpRankListResponse res) {

		// leagueを取得
		String league = (String) sv.getParamsMap().get(ParamsEnum.league);
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