package jp.brainjuice.pokego.business.service.search.cp;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.search.utils.PokemonGoUtils;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.cp.CpRankResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.CpRank;

@Service
public class CpRankResearchService implements ResearchService<CpRankResponse> {

	private PokemonGoUtils pokemonGoUtils;

	public CpRankResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(SearchValue sv, CpRankResponse res) {

		int iva = sv.get(ParamsEnum.iva, int.class);
		int ivd = sv.get(ParamsEnum.ivd, int.class);
		int ivh = sv.get(ParamsEnum.ivh, int.class);
		GoPokedex goPokedex = sv.getGoPokedex();

		res.setIva(iva);
		res.setIvd(ivd);
		res.setIvh(ivh);
		res.setGoPokedex(goPokedex);

		CpRank cpRank = pokemonGoUtils.getBaseCpRank(goPokedex,iva, ivd, ivh);
		res.setCpRank(cpRank);

	}

}
