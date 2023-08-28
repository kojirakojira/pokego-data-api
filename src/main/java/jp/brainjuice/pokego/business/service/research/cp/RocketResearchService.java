package jp.brainjuice.pokego.business.service.research.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RocketIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RocketSakakiIvRange;
import jp.brainjuice.pokego.web.form.res.research.cp.RocketResponse;

/**
 * ロケット団勝利ボーナスで獲得できるポケモンの最低-最高個体値を算出する。
 *
 * @author saibabanagchampa
 *
 */
@Service
public class RocketResearchService implements ResearchService<RocketResponse> {

	private PokemonGoUtils pokemonGoUtils;

	@Autowired
	public RocketResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(SearchValue sv, RocketResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();
		boolean isSakaki = ((Boolean) sv.get(ParamsEnum.sakaki)).booleanValue();

		// 個体値の振れ幅を取得する。
		IvRange ir = isSakaki ? new RocketSakakiIvRange() : new RocketIvRange();

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

		res.setSakaki(isSakaki);

		res.setMessage("");
	}

}
