package jp.brainjuice.pokego.business.service.search.cp;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.utils.PokemonGoUtils;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.cp.CpRankListResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.CpRank;

@Service
public class CpRankListResearchService implements ResearchService<CpRankListResponse> {

	private PokemonGoUtils pokemonGoUtils;

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
