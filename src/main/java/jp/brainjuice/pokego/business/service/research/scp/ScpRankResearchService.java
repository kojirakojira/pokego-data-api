package jp.brainjuice.pokego.business.service.research.scp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.ScpRankCalculator;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.web.form.res.research.scp.ScpRankResponse;

@Service
public class ScpRankResearchService implements ResearchService<ScpRankResponse> {

	private ScpRankCalculator scpRankCulculator;

	@Autowired
	public ScpRankResearchService(ScpRankCalculator scpRankCulculator) {
		this.scpRankCulculator = scpRankCulculator;
	}

	@Override
	public void exec(SearchValue sv, ScpRankResponse res) {

		int iva = (int) sv.get(ParamsEnum.iva);
		int ivd = (int) sv.get(ParamsEnum.ivd);
		int ivh = (int) sv.get(ParamsEnum.ivh);

		GoPokedex goPokedex = sv.getGoPokedex();

		res.setScpSlRank(scpRankCulculator.getSuperLeagueRank(goPokedex, iva, ivd, ivh));

		res.setScpHlRank(scpRankCulculator.getHyperLeagueRank(goPokedex, iva, ivd, ivh));

		res.setScpMlRank(scpRankCulculator.getMasterLeagueRank(goPokedex, iva, ivd, ivh));
	}

}
