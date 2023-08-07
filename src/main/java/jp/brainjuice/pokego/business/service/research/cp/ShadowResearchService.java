package jp.brainjuice.pokego.business.service.research.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.web.form.res.research.cp.ShadowResponse;

@Service
public class ShadowResearchService implements ResearchService<ShadowResponse> {

	private PokemonGoUtils pokemonGoUtils;

	/** レイド固定値最高 */
	private static final int SHADOW_MAX_IV = 15;

	/** レイド個体値最低 */
	private static final int SHADOW_MIN_IV = 0;

	/** 通常時PL */
	private static final String SHADOW_PL = "8";

	/** 天候ブースト時PL */
	private static final String SHADOW_PL_WB = "13";

	@Autowired
	public ShadowResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(SearchValue sv, ShadowResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();
		res.setMaxCp(pokemonGoUtils.calcCp(goPokedex, SHADOW_MAX_IV, SHADOW_MAX_IV, SHADOW_MAX_IV, SHADOW_PL));
		res.setMinCp(pokemonGoUtils.calcCp(goPokedex, SHADOW_MIN_IV, SHADOW_MIN_IV, SHADOW_MIN_IV, SHADOW_PL));
		res.setWbMaxCp(pokemonGoUtils.calcCp(goPokedex, SHADOW_MAX_IV, SHADOW_MAX_IV, SHADOW_MAX_IV, SHADOW_PL_WB));
		res.setWbMinCp(pokemonGoUtils.calcCp(goPokedex, SHADOW_MIN_IV, SHADOW_MIN_IV, SHADOW_MIN_IV, SHADOW_PL_WB));

		res.setMessage("");
	}

}
