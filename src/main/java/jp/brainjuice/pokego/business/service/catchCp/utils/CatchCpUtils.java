package jp.brainjuice.pokego.business.service.catchCp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.Response;

@Component
public class CatchCpUtils {

	private GoPokedexRepository goPokedexRepository;

	private static final String MSG_MEGA_SELECTED = "メガシンカ（ゲンシカイキ含む）前のポケモンで算出しています。";

	@Autowired
	public CatchCpUtils(GoPokedexRepository goPokedexRepository) {
		this.goPokedexRepository = goPokedexRepository;
	}
	/**
	 * メガ進化後のポケモンの場合、メガシンカ前のポケモンを取得する。
	 * また、レスポンスにメッセージをセットする。
	 *
	 * @param goPokedex
	 * @param res
	 * @return
	 */
	public GoPokedex getGoPokedexForMega(GoPokedex goPokedex, Response res) {

		String pid = goPokedex.getPokedexId();
		boolean isMega = PokemonEditUtils.isMega(pid);

		if (!isMega) {
			return null;
		}

		String befMegaPid = PokemonEditUtils.getPokedexIdBeforeMegaEvo(pid);
		GoPokedex befGp = goPokedexRepository.findById(befMegaPid).get();

		res.setMsgLevel(MsgLevelEnum.warn);
		res.setMessage(MSG_MEGA_SELECTED);

		return befGp;
	}
}
