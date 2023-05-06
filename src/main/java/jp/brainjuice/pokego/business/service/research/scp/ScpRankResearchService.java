package jp.brainjuice.pokego.business.service.research.scp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.ScpRankCalculator;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue.ParamsEnum;
import jp.brainjuice.pokego.web.form.res.research.scp.ScpRankResponse;

@Service
public class ScpRankResearchService implements ResearchService<ScpRankResponse> {

	private ScpRankCalculator scpRankCulculator;

	@Autowired
	public ScpRankResearchService(ScpRankCalculator scpRankCulculator) {
		this.scpRankCulculator = scpRankCulculator;
	}

	@Override
	public void exec(IndividialValue iv, ScpRankResponse res) {

		int iva = (int) iv.get(ParamsEnum.iva);
		int ivd = (int) iv.get(ParamsEnum.ivd);
		int ivh = (int) iv.get(ParamsEnum.ivh);

		GoPokedex goPokedex = iv.getGoPokedex();

		res.setScpSlRank(scpRankCulculator.getSuperLeagueRank(goPokedex, iva, ivd, ivh));

		res.setScpHlRank(scpRankCulculator.getHyperLeagueRank(goPokedex, iva, ivd, ivh));

		res.setScpMlRank(scpRankCulculator.getMasterLeagueRank(goPokedex, iva, ivd, ivh));
	}

}
