package jp.brainjuice.pokego.business.service.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
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

		int iva = ((Integer) sv.get(ParamsEnum.iva)).intValue();
		int ivd = ((Integer) sv.get(ParamsEnum.ivd)).intValue();
		int ivh = ((Integer) sv.get(ParamsEnum.ivh)).intValue();
		GoPokedex goPokedex = sv.getGoPokedex();

		res.setIva(iva);
		res.setIvd(ivd);
		res.setIvh(ivh);
		res.setGoPokedex(goPokedex);

		CpRank cpRank = pokemonGoUtils.getBaseCpRank(goPokedex,iva, ivd, ivh);
		res.setCpRank(cpRank);

	}

}
