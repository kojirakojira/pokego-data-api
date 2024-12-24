package jp.brainjuice.pokego.business.service.scp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.ScpRankCalculator;
import jp.brainjuice.pokego.web.form.res.scp.ScpRankResponse;

@Service
public class ScpRankResearchService implements ResearchService<ScpRankResponse> {

	private ScpRankCalculator scpRankCulculator;

	@Autowired
	public ScpRankResearchService(ScpRankCalculator scpRankCulculator) {
		this.scpRankCulculator = scpRankCulculator;
	}

	@Override
	public void exec(SearchValue sv, ScpRankResponse res) {

		int iva = sv.get(ParamsEnum.iva, int.class);
		int ivd = sv.get(ParamsEnum.ivd, int.class);
		int ivh = sv.get(ParamsEnum.ivh, int.class);

		GoPokedex goPokedex = sv.getGoPokedex();

		res.setScpSlRank(scpRankCulculator.getSuperLeagueRank(goPokedex, iva, ivd, ivh));

		res.setScpHlRank(scpRankCulculator.getHyperLeagueRank(goPokedex, iva, ivd, ivh));

		res.setScpMlRank(scpRankCulculator.getMasterLeagueRank(goPokedex, iva, ivd, ivh));
	}

}
