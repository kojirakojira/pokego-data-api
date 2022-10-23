package jp.brainjuice.pokego.business.service.research.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.web.form.res.research.cp.FRTaskResponse;

@Service
public class FRTaskResearchService implements ResearchService<FRTaskResponse> {

	private PokemonGoUtils pokemonGoUtils;

	/** フィールドリサーチタスク固定値最高 */
	private static final int FR_MAX_IV = 15;

	/** フィールドリサーチタスク個体値最低 */
	private static final int FR_MIN_IV = 10;

	/** 通常時PL */
	private static final String FR_PL = "15";

	/** 天候ブースト時PL */
	private static final String FR_PL_WB = "20";

	@Autowired
	public FRTaskResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(IndividialValue iv, FRTaskResponse res) {

		GoPokedex goPokedex = iv.getGoPokedex();
		res.setMaxCp(pokemonGoUtils.culcCp(goPokedex, FR_MAX_IV, FR_MAX_IV, FR_MAX_IV, FR_PL));
		res.setMinCp(pokemonGoUtils.culcCp(goPokedex, FR_MIN_IV, FR_MIN_IV, FR_MIN_IV, FR_PL));
		res.setWbMaxCp(pokemonGoUtils.culcCp(goPokedex, FR_MAX_IV, FR_MAX_IV, FR_MAX_IV, FR_PL_WB));
		res.setWbMinCp(pokemonGoUtils.culcCp(goPokedex, FR_MIN_IV, FR_MIN_IV, FR_MIN_IV, FR_PL_WB));

		res.setMessage("");
	}

}
