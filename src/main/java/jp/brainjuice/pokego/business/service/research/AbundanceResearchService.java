package jp.brainjuice.pokego.business.service.research;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.memory.TooStrongPokemonList;
import jp.brainjuice.pokego.web.form.res.elem.Color;
import jp.brainjuice.pokego.web.form.res.research.AbundanceResponse;

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
	public void exec(IndividialValue iv, AbundanceResponse res) {

		GoPokedex goPokedex = iv.getGoPokedex();
		// こうげき、ぼうぎょ、HP、タイプ
		res.setGoPokedex(goPokedex);
		// CP(PL40)
		res.setCp40(pokemonGoUtils.calcBaseCp(goPokedex.getAttack(), goPokedex.getDefense(), goPokedex.getHp()));
		// 最大CP
		res.setMaxCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "51"));
		// CP(レイド)
		res.setMinRaidCp(pokemonGoUtils.calcCp(goPokedex, 10, 10, 10, "20"));
		res.setMaxRaidCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "20"));
		// CP(レイド天候ブースト)
		res.setMinWbRaidCp(pokemonGoUtils.calcCp(goPokedex, 10, 10, 10, "25"));
		res.setMaxWbRaidCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "25"));
		// CP(フィールドリサーチ)
		res.setMinFrTaskCp(pokemonGoUtils.calcCp(goPokedex, 10, 10, 10, "15"));
		res.setMaxFrTaskCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "15"));
		// 強ポケ補正の有無
		res.setTooStrong(tooStrongPokemonList.contains(goPokedex.getPokedexId()));

		// ポケモンの色
		res.setColor(new Color(goPokedex));
	}
}
