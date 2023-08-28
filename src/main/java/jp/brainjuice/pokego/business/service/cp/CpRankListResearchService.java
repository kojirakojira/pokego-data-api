package jp.brainjuice.pokego.business.service.cp;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.web.form.res.cp.CpRankListResponse;
import jp.brainjuice.pokego.web.form.res.elem.CpRank;

@Service
public class CpRankListResearchService implements ResearchService<CpRankListResponse> {

	private PokemonGoUtils pokemonGoUtils;

	@Autowired
	public CpRankListResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(SearchValue sv, CpRankListResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();
		res.setGoPokedex(goPokedex);

		ArrayList<CpRank> cpRankList = pokemonGoUtils.getBaseCpRankList(goPokedex);
		res.setCpRankList(cpRankList);
	}

}
