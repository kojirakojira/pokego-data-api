package jp.brainjuice.pokego.business.service.catchCp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.FRTaskIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRange;
import jp.brainjuice.pokego.web.form.res.catchCp.FRTaskResponse;

@Service
public class FRTaskResearchService implements ResearchService<FRTaskResponse> {

	private PokemonGoUtils pokemonGoUtils;

	@Autowired
	public FRTaskResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(SearchValue sv, FRTaskResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();

		// 個体値の振れ幅を取得する。
		IvRange ir = new FRTaskIvRange();

		int maxIv = ir.getMaxIv();
		int minIv = ir.getMinIv();
		String pl = ir.getMaxPl();

		// 通常
		res.setMaxCp(pokemonGoUtils.calcCp(goPokedex, maxIv, maxIv, maxIv, pl));
		res.setMinCp(pokemonGoUtils.calcCp(goPokedex, minIv, minIv, minIv, pl));

		res.setMessage("");
	}

}
