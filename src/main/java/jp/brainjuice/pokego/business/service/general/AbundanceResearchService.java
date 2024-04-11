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
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.EggsIvRange;
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

		// CP算出用のGoPokedex。メガシンカの場合は、メガシンカ後のポケモンに置き換えて算出する。
		GoPokedex cpTargetGp = catchCpUtils.getGoPokedexForMega(goPokedex).orElse(goPokedex);

		// CP(レイド)
		IvRangeCp raid = catchCpUtils.getIvRangeCp(cpTargetGp, new RaidIvRange());
		res.setRaid(new CatchCp(raid, null));
		// CP(ロケット団勝利ボーナス）
		IvRangeCp rocket = catchCpUtils.getIvRangeCp(cpTargetGp, new RocketIvRange());
		res.setRocket(new CatchCp(rocket, null));

		// CP(フィールドリサーチ)
		IvRangeCp fRTask = catchCpUtils.getIvRangeCp(cpTargetGp, new FRTaskIvRange());
		res.setFRTask(new CatchCp(fRTask, null));
		// CP(タマゴ)
		IvRangeCp egg = catchCpUtils.getIvRangeCp(cpTargetGp, new EggsIvRange());
		res.setEgg(new CatchCp(egg, null));

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
