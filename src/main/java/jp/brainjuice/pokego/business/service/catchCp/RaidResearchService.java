package jp.brainjuice.pokego.business.service.catchCp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RaidIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RaidShadowIvRange;
import jp.brainjuice.pokego.web.form.res.catchCp.RaidResponse;

@Service
public class RaidResearchService implements ResearchService<RaidResponse> {

	private PokemonGoUtils pokemonGoUtils;

	@Autowired
	public RaidResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(SearchValue sv, RaidResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();
		boolean isShadow = ((Boolean) sv.get(ParamsEnum.shadow)).booleanValue();

		// 個体値の振れ幅を取得する。
		IvRange ir = isShadow ? new RaidShadowIvRange() : new RaidIvRange();

		int maxIv = ir.getMaxIv();
		int minIv = ir.getMinIv();
		String pl = ir.getMaxPl();
		String plWb = ir.getMaxPlWb();

		// 通常
		res.setMaxCp(pokemonGoUtils.calcCp(goPokedex, maxIv, maxIv, maxIv, pl));
		res.setMinCp(pokemonGoUtils.calcCp(goPokedex, minIv, minIv, minIv, pl));
		// 天候ブースト
		res.setWbMaxCp(pokemonGoUtils.calcCp(goPokedex, maxIv, maxIv, maxIv, plWb));
		res.setWbMinCp(pokemonGoUtils.calcCp(goPokedex, minIv, minIv, minIv, plWb));

		res.setShadow(isShadow);

		res.setMessage("");
	}

}
