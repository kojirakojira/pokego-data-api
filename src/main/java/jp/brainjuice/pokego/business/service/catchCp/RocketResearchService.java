package jp.brainjuice.pokego.business.service.catchCp;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.catchCp.utils.CatchCpUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RocketIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RocketSakakiIvRange;
import jp.brainjuice.pokego.web.form.res.catchCp.RocketResponse;

/**
 * ロケット団勝利ボーナスで獲得できるポケモンの最低-最高個体値を算出する。
 *
 * @author saibabanagchampa
 *
 */
@Service
public class RocketResearchService implements ResearchService<RocketResponse> {

	private PokemonGoUtils pokemonGoUtils;

	private CatchCpUtils catchCpUtils;

	@Autowired
	public RocketResearchService(
			PokemonGoUtils pokemonGoUtils,
			CatchCpUtils catchCpUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
		this.catchCpUtils = catchCpUtils;
	}

	@Override
	public void exec(SearchValue sv, RocketResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();
		boolean isSakaki = ((Boolean) sv.get(ParamsEnum.sakaki)).booleanValue();

		{
			// メガシンカ後のポケモンの場合は、メガシンカ前のポケモンを取得する。
			Optional<GoPokedex> befMegaGp = catchCpUtils.getGoPokedexForMega(goPokedex, res);
			if (!befMegaGp.isPresent()) {
				// nullでなかったらgoPokedexはメガシンカ後。メガシンカ前のポケモンで後続処理を進める。
				goPokedex = befMegaGp.get();
				res.setMega(true);
				res.setBefMegaGp(befMegaGp.get());
			}
		}

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
	}

}
