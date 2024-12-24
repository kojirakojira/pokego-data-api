package jp.brainjuice.pokego.business.service.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.web.form.res.cp.CpRankResponse;
import jp.brainjuice.pokego.web.form.res.elem.CpRank;

@Service
public class CpRankResearchService implements ResearchService<CpRankResponse> {

	private PokemonGoUtils pokemonGoUtils;

	@Autowired
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
