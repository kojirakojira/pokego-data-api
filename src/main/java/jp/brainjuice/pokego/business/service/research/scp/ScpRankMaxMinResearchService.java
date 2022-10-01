package jp.brainjuice.pokego.business.service.research.scp;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.ScpRankCulculator;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.web.form.res.elem.ScpRank;
import jp.brainjuice.pokego.web.form.res.research.scp.ScpRankMaxMinResponse;

@Service
public class ScpRankMaxMinResearchService implements ResearchService<ScpRankMaxMinResponse> {

	private ScpRankCulculator scpRankCulculator;

	@Autowired
	public ScpRankMaxMinResearchService(ScpRankCulculator scpRankCulculator) {
		this.scpRankCulculator = scpRankCulculator;
	}

	@Override
	public void exec(IndividialValue iv, ScpRankMaxMinResponse res) {

		GoPokedex goPokedex = iv.getGoPokedex();
		{
			ArrayList<ScpRank> slList = scpRankCulculator.getSuperLeagueSummary(goPokedex);
			res.setScpSlRankMax(slList.get(0));
			res.setScpSlRankMin(slList.get(slList.size() - 1));
		}
		{
			ArrayList<ScpRank> hlList = scpRankCulculator.getHyperLeagueSummary(goPokedex);
			res.setScpHlRankMax(hlList.get(0));
			res.setScpHlRankMin(hlList.get(hlList.size() - 1));
		}
		{
			ArrayList<ScpRank> mlList = scpRankCulculator.getMasterLeagueSummary(goPokedex);
			res.setScpMlRankMax(mlList.get(0));
			res.setScpMlRankMin(mlList.get(mlList.size() - 1));
		}

		res.setMessage("成功！！");
	}

}
