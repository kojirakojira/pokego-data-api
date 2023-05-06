package jp.brainjuice.pokego.business.service.research.scp;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.ScpRankCalculator;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue.ParamsEnum;
import jp.brainjuice.pokego.web.form.res.elem.ScpRank;
import jp.brainjuice.pokego.web.form.res.research.scp.ScpRankListResponse;

@Service
public class ScpRankListResearchService implements ResearchService<ScpRankListResponse> {

	private ScpRankCalculator scpRankCulculator;

	@Autowired
	public ScpRankListResearchService(
			ScpRankCalculator scpRankCulculator) {
		this.scpRankCulculator = scpRankCulculator;
	}

	@Override
	public void exec(IndividialValue iv, ScpRankListResponse res) {

		// leagueを取得
		String league = (String) iv.getParamsMap().get(ParamsEnum.league);
		// scpRankListを生成
		ArrayList<ScpRank> scpRankList = scpRankCulculator.getSummary(
				iv.getGoPokedex(),
				league);

		// レスポンスのセット
		res.setScpRankList(scpRankList);
		res.setLeague(league);

		res.setMessage("");
	}

}
