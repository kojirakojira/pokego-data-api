package jp.brainjuice.pokego.business.service.general;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.Type.TypeColorEnum;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.catchCp.utils.CatchCpUtils;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.ScpRankCalculator;
import jp.brainjuice.pokego.business.service.utils.dto.GoPokedexAndCpPl;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.EggsIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.FrTaskIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRangeCp;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RaidIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RocketIvRange;
import jp.brainjuice.pokego.business.service.utils.evo.EvolutionProvider;
import jp.brainjuice.pokego.web.form.res.elem.CatchCp;
import jp.brainjuice.pokego.web.form.res.elem.Color;
import jp.brainjuice.pokego.web.form.res.elem.ScpRank;
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
	
	private GoPokedexRepository goPokedexRepository;

	private EvolutionProvider evolutionProvider;
	
	private ScpRankCalculator scpRankCalculator;

	public AbundanceResearchService(
			PokemonGoUtils pokemonGoUtils,
			CatchCpUtils catchCpUtils,
			GoPokedexRepository goPokedexRepository,
			EvolutionProvider evolutionProvider,
			ScpRankCalculator scpRankCalculator) {
		this.pokemonGoUtils = pokemonGoUtils;
		this.catchCpUtils = catchCpUtils;
		
		this.goPokedexRepository = goPokedexRepository;
		this.evolutionProvider = evolutionProvider;
		this.scpRankCalculator = scpRankCalculator;
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
		IvRangeCp frTask = catchCpUtils.getIvRangeCp(cpTargetGp, new FrTaskIvRange());
		res.setFrTask(new CatchCp(frTask, null));
		// CP(タマゴ)
		IvRangeCp egg = catchCpUtils.getIvRangeCp(cpTargetGp, new EggsIvRange());
		res.setEgg(new CatchCp(egg, null));

		// 強ポケ補正の有無
		res.setTooStrong(goPokedex.isTooStrong());

		// ポケモンの色
		// タイプ1の色を設定
		final TypeColorEnum c1 = TypeColorEnum.valueOf(goPokedex.getType1());
		res.setType1Color(new Color(c1.getR(), c1.getG(), c1.getB()));
		// タイプ2の色を設定
		if (goPokedex.getType2() != null) {
			final TypeColorEnum c2 = TypeColorEnum.valueOf(goPokedex.getType2().name());
			res.setType2Color(new Color(c2.getR(), c2.getG(), c2.getB()));
		}

		boolean isMega = PokemonEditUtils.isMega(goPokedex.getPokedexId());
		res.setMega(isMega);
		
		// スーパーリーグ、ハイパーリーグ制限内最大CP
		setLeagueSafeCp(goPokedex, res);
	}
	
	/**
	 * 制限内最大CP
	 * @param pokedexId
	 * @param res
	 */
	private void setLeagueSafeCp(GoPokedex goPokedex, AbundanceResponse res) {
		
		String pokedexId = goPokedex.getPokedexId();
		// 最終進化のポケモンを取得。検索元と検索後のpokedexIdが一致する場合は検索結果なしの扱い
		List<String> evolList = evolutionProvider.getLeafCanGoEvol(pokedexId)
				.stream()
				.filter(pid -> !pokedexId.equals(pid))
				.toList();
		
		if (evolList.isEmpty()) {
			// 進化後が存在しない場合
			res.setSuperLeagueSafeCpList(new ArrayList<>());
			res.setHyperLeagueSafeCpList(new ArrayList<>());
			return;
		}
		
		List<GoPokedex> goPokedexList = goPokedexRepository.findAllById(evolList);
		
		List<GoPokedexAndCpPl> slLeagueSafeCp = goPokedexList.stream()
				.map(gp -> {
					// 最終進化の最低個体値でスーパーリーグ制限にひっかからないPLを取得し、そのPLから進化前の状態のCPを求める。
					ScpRank slScpRank = scpRankCalculator.createScpRank(gp, 0, 0, 0, ScpRankCalculator.SL_CP_LIMIT_PREDICATE);
					String pl = slScpRank.getPl();
					int cp = pokemonGoUtils.calcCp(goPokedex, 0, 0, 0, pl);
					return new GoPokedexAndCpPl(gp, cp, pl);
				})
				.toList();
		List<GoPokedexAndCpPl> hlLeagueSafeCp = goPokedexList.stream()
				.map(gp -> {
					ScpRank hlScpRank = scpRankCalculator.createScpRank(gp, 0, 0, 0, ScpRankCalculator.HL_CP_LIMIT_PREDICATE);
					String pl = hlScpRank.getPl();
					int cp = pokemonGoUtils.calcCp(goPokedex, 0, 0, 0, pl);
					return new GoPokedexAndCpPl(gp, cp, pl);
				})
				.toList();
		res.setSuperLeagueSafeCpList(slLeagueSafeCp);
		res.setHyperLeagueSafeCpList(hlLeagueSafeCp);
	}
}
