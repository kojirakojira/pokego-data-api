package jp.brainjuice.pokego.business.service.research.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue.ParamsEnum;
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

		int iva = ((Integer) iv.get(ParamsEnum.iva)).intValue();
		int ivd = ((Integer) iv.get(ParamsEnum.ivd)).intValue();
		int ivh = ((Integer) iv.get(ParamsEnum.ivh)).intValue();
		GoPokedex goPokedex = iv.getGoPokedex();

		res.setIva(iva);
		res.setIvd(ivd);
		res.setIvh(ivh);
		res.setGoPokedex(goPokedex);

		CpRank cpRank = pokemonGoUtils.getBaseCpRank(goPokedex,iva, ivd, ivh);
		res.setCpRank(cpRank);

	}

}
