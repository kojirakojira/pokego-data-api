package jp.brainjuice.pokego.business.service.catchCp.utils;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
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

		String pid = goPokedex.getPokedexId();
		boolean isMega = PokemonEditUtils.isMega(pid);

		if (!isMega) {
			return Optional.empty();
		}

		String befMegaPid = PokemonEditUtils.getPokedexIdBeforeMegaEvo(pid);
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

		if (!befGp.isPresent()) {
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
		String pl = ir.getMaxPl();

		// 通常
		ivRangeCp.setMax(pokemonGoUtils.calcCp(goPokedex, maxIv, maxIv, maxIv, pl));
		ivRangeCp.setMin(pokemonGoUtils.calcCp(goPokedex, minIv, minIv, minIv, pl));

		Integer maxIvWb = ir.getMaxIvWb();
		Integer minIvWb = ir.getMinIvWb();
		String plWb = ir.getMaxPlWb();
		// 天候ブースト
		if (maxIvWb != null && minIvWb != null && plWb != null) {
			ivRangeCp.setWbMax(pokemonGoUtils.calcCp(goPokedex, maxIvWb, maxIvWb, maxIvWb, plWb));
			ivRangeCp.setWbMin(pokemonGoUtils.calcCp(goPokedex, minIvWb, minIvWb, minIvWb, plWb));
		}

		return ivRangeCp;
	}
}
