package jp.brainjuice.pokego.business.service.research.pl;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonEnum;
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

		final int cp = (int) iv.getParamsMap().get(PokemonEnum.cp.name());
		if (cp <= 10) {
			setPlLessThan10Cp(at, df, hp, cp, res);
			return;
		}

		setPl(at, df, hp, cp, res);
	}

	/**
	 * CPが一致するPLを取得します。
	 *
	 * @param at
	 * @param df
	 * @param hp
	 * @param cp
	 * @param res
	 */
	private void setPl(int at, int df, int hp, int cp, PlResponse res) {

		String pl = null;
		DecimalFormat plFormat = new DecimalFormat("0.#");
		// PL51からPL1までループ
		for (int tpl = 510; tpl >= 10; tpl-=5) {
			pl = plFormat.format(tpl / 10.0);
			// PLに応じたCPを取得する。
			int plcp = pokemonGoUtils.culcCp(at, df, hp, pl);

			if (cp == plcp) {
				res.setPl(pl);
				break;
			}
		}

		res.setMessage("存在しないステータスを指定しています。");
	}

	/**
	 * CPが10の場合は、PLが複数存在する場合があります。
	 *
	 * @param at
	 * @param df
	 * @param hp
	 * @param cp
	 * @param res
	 */
	private void setPlLessThan10Cp(int at, int df, int hp, int cp, PlResponse res) {

		ArrayList<String> plList = new ArrayList<String>();
		DecimalFormat plFormat = new DecimalFormat("0.#");
		for (int tpl = 10; tpl <= 510; tpl+=5) {
			String pl = plFormat.format(tpl / 10.0);
			// PLに応じたCPを取得する。
			int plcp = pokemonGoUtils.culcCp(at, df, hp, pl);

			if (plcp <= 10) {
				plList.add(pl);
			} else {
				// PLに応じたCPが10を超えたらループを抜ける。
				break;
			}
		}

		if (plList.size() == 1) {
			res.setPl(plList.get(0));
		} else if (1 < plList.size()) {
			res.setPl(plList.get(0) + "～" + plList.get(plList.size() - 1));
		}
	}

}
