package jp.brainjuice.pokego.business.service.research.cp;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.web.form.res.elem.CpRank;
import jp.brainjuice.pokego.web.form.res.research.cp.CpRankListResponse;

@Service
public class CpRankListResearchService implements ResearchService<CpRankListResponse> {

	private PokemonGoUtils pokemonGoUtils;

	@Autowired
	public CpRankListResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(IndividialValue iv, CpRankListResponse res) {
		ArrayList<CpRank> cpRankList = pokemonGoUtils.getBaseCpRankList(iv.getGoPokedex());
		res.setCpRankList(cpRankList);

		res.setMessage("");
	}

}
