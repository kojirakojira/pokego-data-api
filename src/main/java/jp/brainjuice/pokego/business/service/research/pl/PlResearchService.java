package jp.brainjuice.pokego.business.service.research.pl;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue.ParamsEnum;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.research.pl.PlResponse;

@Service
public class PlResearchService implements ResearchService<PlResponse> {

	private PokemonGoUtils pokemonGoUtils;

	public PlResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(IndividialValue iv, PlResponse res) {

		GoPokedex goPokedex = iv.getGoPokedex();

		final int at = goPokedex.getAttack() + iv.getIva();
		final int df = goPokedex.getDefense() + iv.getIvd();
		final int hp = goPokedex.getHp() + iv.getIvh();

		String pl = null;
		final int cp = (int) iv.get(ParamsEnum.cp);
		if (cp <= 10) {
			pl = getPlLessThan10Cp(at, df, hp, cp);
		} else {
			pl = getPl(at, df, hp, cp);
		}
		res.setPl(pl);

		if (pl == null) {
			res.setMessage("存在しないステータスを指定しています。");
			res.setMsgLevel(MsgLevelEnum.error);
		}
	}

	/**
	 * CPが一致するPLを取得します。
	 *
	 * @param at
	 * @param df
	 * @param hp
	 * @param cp
	 * @return
	 */
	private String getPl(int at, int df, int hp, int cp) {

		String pl = null;
		DecimalFormat plFormat = new DecimalFormat("0.#");
		// PL51からPL1までループ
		for (int tpl = 510; tpl >= 10; tpl-=5) {
			pl = plFormat.format(tpl / 10.0);
			// PLに応じたCPを取得する。
			int plcp = pokemonGoUtils.calcCp(at, df, hp, pl);

			if (cp == plcp) {
				return pl;
			}
		}
		return null;

	}

	/**
	 * CPが10の場合は、PLが複数存在する場合があります。
	 *
	 * @param at
	 * @param df
	 * @param hp
	 * @param cp
	 * @return
	 */
	private String getPlLessThan10Cp(int at, int df, int hp, int cp) {

		String retPl = null;
		ArrayList<String> plList = new ArrayList<String>();
		DecimalFormat plFormat = new DecimalFormat("0.#");
		for (int tpl = 10; tpl <= 510; tpl+=5) {
			String pl = plFormat.format(tpl / 10.0);
			// PLに応じたCPを取得する。
			int plcp = pokemonGoUtils.calcCp(at, df, hp, pl);

			if (plcp <= 10) {
				plList.add(pl);
			} else {
				// PLに応じたCPが10を超えたらループを抜ける。
				break;
			}
		}

		if (plList.size() == 1) {
			retPl = plList.get(0);
		} else if (1 < plList.size()) {
			retPl = plList.get(0) + "～" + plList.get(plList.size() - 1);
		}

		return retPl;
	}

}
