package jp.brainjuice.pokego.business.service.catchCp.utils;

import java.util.Optional;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRangeCp;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.Response;

@Component
public class CatchCpUtils {

	private GoPokedexRepository goPokedexRepository;

	private PokemonGoUtils pokemonGoUtils;

	private static final String MSG_MEGA_SELECTED = "メガシンカ（ゲンシカイキ含む）前のポケモンで算出しています。";

	public CatchCpUtils(
			GoPokedexRepository goPokedexRepository,
			PokemonGoUtils pokemonGoUtils) {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonGoUtils = pokemonGoUtils;
	}

	/**
	 * メガ進化後のポケモンの場合、メガシンカ前のポケモンを取得する。
	 *
	 * @param goPokedex
	 * @param res
	 * @return
	 */
	public Optional<GoPokedex> getGoPokedexForMega(GoPokedex goPokedex) {

		if (!PokemonEditUtils.isMega(goPokedex)) {
			return Optional.empty();
		}

		String befMegaPid = goPokedex.getPreMegaPokedexId();
		return goPokedexRepository.findById(befMegaPid);
	}

	/**
	 * メガ進化後のポケモンの場合、メガシンカ前のポケモンを取得する。
	 * また、レスポンスにメッセージをセットする。
	 *
	 * @param goPokedex
	 * @param res
	 * @return
	 */
	public Optional<GoPokedex> getGoPokedexForMega(GoPokedex goPokedex, Response res) {

		Optional<GoPokedex> befGp = getGoPokedexForMega(goPokedex);

		if (befGp.isPresent()) {
			res.setMsgLevel(MsgLevelEnum.warn);
			res.setMessage(MSG_MEGA_SELECTED);
		}

		return befGp;
	}

	/**
	 * 個体値の振れ幅からCPを算出する。
	 *
	 * @param goPokedex
	 * @param ir
	 * @return
	 * @see IvRange
	 */
	public IvRangeCp getIvRangeCp(GoPokedex goPokedex, IvRange ir) {
		IvRangeCp ivRangeCp = new IvRangeCp();

		int maxIv = ir.getMaxIv();
		int minIv = ir.getMinIv();
		String maxPl = ir.getMaxPl();
		String minPl = ir.getMinPl();

		// 通常
		ivRangeCp.setMax(pokemonGoUtils.calcCp(goPokedex, maxIv, maxIv, maxIv, maxPl));
		ivRangeCp.setMin(pokemonGoUtils.calcCp(goPokedex, minIv, minIv, minIv, minPl));

		Integer maxIvWb = ir.getMaxIvWb();
		Integer minIvWb = ir.getMinIvWb();
		String maxPlWb = ir.getMaxPlWb();
		String minPlWb = ir.getMinPlWb();
		// 天候ブースト
		if (maxIvWb != null && minIvWb != null && maxPlWb != null && minPlWb != null) {
			ivRangeCp.setWbMax(pokemonGoUtils.calcCp(goPokedex, maxIvWb, maxIvWb, maxIvWb, maxPlWb));
			ivRangeCp.setWbMin(pokemonGoUtils.calcCp(goPokedex, minIvWb, minIvWb, minIvWb, minPlWb));
		}

		return ivRangeCp;
	}
}
