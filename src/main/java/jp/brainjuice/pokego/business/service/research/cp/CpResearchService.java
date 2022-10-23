package jp.brainjuice.pokego.business.service.research.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.web.form.res.research.cp.CpResponse;

@Service
public class CpResearchService implements ResearchService<CpResponse> {

	private PokemonGoUtils pokemonGoUtils;

	@Autowired
	public CpResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	/**
	 * CPを算出します。
	 */
	@Override
	public void exec(IndividialValue iv, CpResponse cpResponse) {

		int cp = pokemonGoUtils.culcCp(
				iv.getGoPokedex(),
				iv.getIva(),
				iv.getIvd(),
				iv.getIvh(),
				iv.getPl());

		cpResponse.setCp(cp);

		cpResponse.setMessage("");
	}

}
