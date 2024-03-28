package jp.brainjuice.pokego.business.service.general;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.Type.TypeColorEnum;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.catchCp.utils.CatchCpUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.EggIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.FRTaskIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRangeCp;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RaidIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RocketIvRange;
import jp.brainjuice.pokego.business.service.utils.memory.TooStrongPokemonList;
import jp.brainjuice.pokego.web.form.res.elem.CatchCp;
import jp.brainjuice.pokego.web.form.res.elem.Color;
import jp.brainjuice.pokego.web.form.res.general.AbundanceResponse;

/**
 * ポケモンの総合的な情報を取得するためのサービスクラスです。
 *
 * @author saibabanagchampa
 *
 */
@Service
public class AbundanceResearchService implements ResearchService<AbundanceResponse> {

	private PokemonGoUtils pokemonGoUtils;

	private CatchCpUtils catchCpUtils;

	private TooStrongPokemonList tooStrongPokemonList;

	@Autowired
	public AbundanceResearchService(
			PokemonGoUtils pokemonGoUtils,
			CatchCpUtils catchCpUtils,
			TooStrongPokemonList tooStrongPokemonList) {
		this.pokemonGoUtils = pokemonGoUtils;
		this.catchCpUtils = catchCpUtils;
		this.tooStrongPokemonList = tooStrongPokemonList;
	}

	@Override
	public void exec(SearchValue sv, AbundanceResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();
		// こうげき、ぼうぎょ、HP、タイプ
		res.setGoPokedex(goPokedex);
		// CP(PL40)
		res.setCp40(pokemonGoUtils.calcBaseCp(goPokedex.getAttack(), goPokedex.getDefense(), goPokedex.getHp()));
		// CP(PL50)
		res.setCp50(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "50"));
		// 最大CP
		res.setMaxCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "51"));
		// CP(レイド)
		IvRangeCp raid = catchCpUtils.getIvRangeCp(goPokedex, new RaidIvRange());
		res.setRaid(new CatchCp(raid.getMin(), raid.getMax(), raid.getWbMin(), raid.getWbMax(), null, 0, 0));
		// CP(ロケット団勝利ボーナス）
		IvRangeCp rocket = catchCpUtils.getIvRangeCp(goPokedex, new RocketIvRange());
		res.setRocket(new CatchCp(rocket.getMin(), rocket.getMax(), rocket.getWbMin(), rocket.getWbMax(), null, 0, 0));

		// CP(フィールドリサーチ)
		IvRangeCp fRTask = catchCpUtils.getIvRangeCp(goPokedex, new FRTaskIvRange());
		res.setFRTask(new CatchCp(fRTask.getMin(), fRTask.getMax(), fRTask.getWbMin(), fRTask.getWbMax(), null, 0, 0));
		// CP(タマゴ)
		IvRangeCp egg = catchCpUtils.getIvRangeCp(goPokedex, new EggIvRange());
		res.setEgg(new CatchCp(egg.getMin(), egg.getMax(), egg.getWbMin(), egg.getWbMax(), null, 0, 0));

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

		boolean isMega = PokemonEditUtils.isMega(goPokedex.getPokedexId());
		res.setMega(isMega);
	}
}
