package jp.brainjuice.pokego.business.service.research.others;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.Type.TypeColorEnum;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.memory.TooStrongPokemonList;
import jp.brainjuice.pokego.web.form.res.elem.Color;
import jp.brainjuice.pokego.web.form.res.research.others.AbundanceResponse;

/**
 * ポケモンの総合的な情報を取得するためのサービスクラスです。
 *
 * @author saibabanagchampa
 *
 */
@Service
public class AbundanceResearchService implements ResearchService<AbundanceResponse> {

	private PokemonGoUtils pokemonGoUtils;

	private TooStrongPokemonList tooStrongPokemonList;

	@Autowired
	public AbundanceResearchService(
			PokemonGoUtils pokemonGoUtils,
			TooStrongPokemonList tooStrongPokemonList) {
		this.pokemonGoUtils = pokemonGoUtils;
		this.tooStrongPokemonList = tooStrongPokemonList;
	}

	@Override
	public void exec(SearchValue sv, AbundanceResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();
		// こうげき、ぼうぎょ、HP、タイプ
		res.setGoPokedex(goPokedex);
		// CP(PL40)
		res.setCp40(pokemonGoUtils.calcBaseCp(goPokedex.getAttack(), goPokedex.getDefense(), goPokedex.getHp()));
		// 最大CP
		res.setMaxCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "51"));
		// CP(レイド)
		res.setMinRaidCp(pokemonGoUtils.calcCp(goPokedex, 10, 10, 10, "20"));
		res.setMaxRaidCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "20"));
		res.setMinWbRaidCp(pokemonGoUtils.calcCp(goPokedex, 10, 10, 10, "25"));
		res.setMaxWbRaidCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "25"));
		// CP(シャドウ）
		res.setMinShadowCp(pokemonGoUtils.calcCp(goPokedex, 0, 0, 0, "8"));
		res.setMaxShadowCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "8"));
		res.setMinWbShadowCp(pokemonGoUtils.calcCp(goPokedex, 0, 0, 0, "13"));
		res.setMaxWbShadowCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "13"));

		int minFrTaskAndEggCp = pokemonGoUtils.calcCp(goPokedex, 10, 10, 10, "15");
		int maxFrTaskAndEggCp = pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "15");
		// CP(フィールドリサーチ)
		res.setMinFrTaskCp(minFrTaskAndEggCp);
		res.setMaxFrTaskCp(maxFrTaskAndEggCp);
		// CP(タマゴ)
		res.setMinEggCp(minFrTaskAndEggCp);
		res.setMaxEggCp(maxFrTaskAndEggCp);
		// 強ポケ補正の有無
		res.setTooStrong(tooStrongPokemonList.contains(goPokedex.getPokedexId()));

		// ポケモンの色
		// タイプ1の色を設定
		final TypeColorEnum c1 = TypeColorEnum.getTypeColorForJpn(goPokedex.getType1());
		res.setType1Color(new Color(c1.getR(), c1.getG(), c1.getB()));
		// タイプ2の色を設定
		if (!StringUtils.isEmpty(goPokedex.getType2())) {
			final TypeColorEnum c2 = TypeColorEnum.getTypeColorForJpn(goPokedex.getType2());
			res.setType2Color(new Color(c2.getR(), c2.getG(), c2.getB()));
		}
	}
}
