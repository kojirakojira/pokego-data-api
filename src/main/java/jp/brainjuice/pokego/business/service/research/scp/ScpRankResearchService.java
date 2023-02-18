package jp.brainjuice.pokego.business.service.research.scp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.ScpRankCulculator;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue.ParamsEnum;
import jp.brainjuice.pokego.web.form.res.research.scp.ScpRankResponse;

@Service
public class ScpRankResearchService implements ResearchService<ScpRankResponse> {

	private ScpRankCulculator scpRankCulculator;

	@Autowired
	public ScpRankResearchService(ScpRankCulculator scpRankCulculator) {
		this.scpRankCulculator = scpRankCulculator;
	}

	@Override
	public void exec(IndividialValue iv, ScpRankResponse res) {

		int iva = (int) iv.get(ParamsEnum.iva);
		int ivd = (int) iv.get(ParamsEnum.ivd);
		int ivh = (int) iv.get(ParamsEnum.ivh);

		GoPokedex goPokedex = iv.getGoPokedex();
		scpRankCulculator.getSuperLeagueSummary(goPokedex).forEach(scpl -> {
			if (iva == scpl.getIva() && ivd == scpl.getIvd() && ivh == scpl.getIvh()) {
				res.setScpSlRank(scpl);
			}
		});

		scpRankCulculator.getHyperLeagueSummary(goPokedex).forEach(scpl -> {
			if (iva == scpl.getIva() && ivd == scpl.getIvd() && ivh == scpl.getIvh()) {
				res.setScpHlRank(scpl);
			}
		});

		scpRankCulculator.getMasterLeagueSummary(goPokedex).forEach(scpl -> {
			if (iva == scpl.getIva() && ivd == scpl.getIvd() && ivh == scpl.getIvh()) {
				res.setScpMlRank(scpl);
			}
		});

		res.setMessage("");
	}

}
