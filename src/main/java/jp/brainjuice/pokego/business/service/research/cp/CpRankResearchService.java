package jp.brainjuice.pokego.business.service.research.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.web.form.res.elem.CpRank;
import jp.brainjuice.pokego.web.form.res.research.cp.CpRankResponse;

@Service
public class CpRankResearchService implements ResearchService<CpRankResponse> {

	private PokemonGoUtils pokemonGoUtils;

	@Autowired
	public CpRankResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(IndividialValue iv, CpRankResponse res) {

		CpRank cpRank = pokemonGoUtils.getBaseCpRank(
				iv.getGoPokedex(),
				iv.getIva(),
				iv.getIvd(),
				iv.getIvh());
		res.setCpRank(cpRank);

		res.setMessage("");

	}

}
