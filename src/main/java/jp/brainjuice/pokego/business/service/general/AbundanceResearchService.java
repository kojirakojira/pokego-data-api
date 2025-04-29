package jp.brainjuice.pokego.business.service.general;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.constant.Type.TypeColorEnum;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.Evolution;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.catchCp.utils.CatchCpUtils;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.ScpRankCalculator;
import jp.brainjuice.pokego.business.service.utils.dto.GoPokedexAndCpPl;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.DynamaxIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.EggsIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.FrTaskIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRangeCp;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RaidIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RocketIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.WildIvRange;
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
	
	/** {0}からメガシンカ */
	private final String PRE_MEGA_MSG = "{0}からメガシンカ";
	/** {0}に進化させればメガシンカ可能 */
	private final String CAN_MEGA_IF_EVOL_MSG = "{0}に進化させればメガシンカ可能";
	/** 区切り文字（か） */
	private final String CAN_MEGA_IF_EVOL_MSG_OR_PARTS = "か、";
	/** メガシンカ可能 */
	private final String CAN_MEGA_MSG = "メガシンカ可能";
	/** メガシンカ可能 */
	private final String CANT_MEGA_MSG = "メガシンカ不可";
	/** ダイマックス可能 */
	private final String CAN_DYNAMAX_MSG = "ダイマックス可能";
	/** ダイマックス不可 */
	private final String CANT_DYNAMAX_MSG = "ダイマックス不可";
	/** キョダイマックス可能 */
	private final String CAN_GIGANTAMAX_MSG = "キョダイマックス可能";
	/** キョダイマックス不可 */
	private final String CANT_GIGANTAMAX_MSG = "キョダイマックス不可";

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
		String pokedexId = goPokedex.getPokedexId();

		List<Evolution> evolTreeList = evolutionProvider.getEvolTreeAndMegaList(pokedexId);
		List<GoPokedex> evolTreeGpList = goPokedexRepository.findAllById(
				evolTreeList.stream()
				.map(Evolution::getPokedexId)
				.toList()
				);
		
		// こうげき、ぼうぎょ、HP、タイプ
		res.setGoPokedex(goPokedex);
		// CP(PL40)
		res.setCp40(pokemonGoUtils.calcBaseCp(goPokedex.getAttack(), goPokedex.getDefense(), goPokedex.getHp()));
		// CP(PL50)
		res.setCp50(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "50"));
		// 最大CP
		res.setMaxCp(pokemonGoUtils.calcCp(goPokedex, 15, 15, 15, "51"));
		// CP算出用のGoPokedex。メガシンカの場合は、メガシンカ前のポケモンに置き換えて算出する。
		GoPokedex cpTargetGp = goPokedex;
		if (PokemonEditUtils.isMega(goPokedex)) {
			cpTargetGp = evolTreeGpList.stream()
					.filter(gp -> gp.getPokedexId().equals(goPokedex.getPreMegaPokedexId()))
					.findFirst().orElseThrow();
		}

		// CP(野生)
		IvRangeCp wild = catchCpUtils.getIvRangeCp(cpTargetGp, new WildIvRange());
		res.setWild(new CatchCp(wild, null));
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
		setEgg(cpTargetGp, evolTreeList, evolTreeGpList, res);
		
		// ダイマックス、キョダイマックス
		if (goPokedex.isDynamaxImplFlg() || goPokedex.isGigantamaxImplFlg()) {
			IvRangeCp dynamax = catchCpUtils.getIvRangeCp(goPokedex, new DynamaxIvRange());
			CatchCp dynamaxCatchCp = new CatchCp(dynamax, null);

			res.setDynamax(goPokedex.isDynamaxImplFlg() ? dynamaxCatchCp : null);

			res.setGigantamax(goPokedex.isGigantamaxImplFlg() ? dynamaxCatchCp : null);
		}
		res.setDynamaxMsg(goPokedex.isDynamaxImplFlg() ? CAN_DYNAMAX_MSG : CANT_DYNAMAX_MSG);
		res.setGigantamaxMsg(goPokedex.isGigantamaxImplFlg() ? CAN_GIGANTAMAX_MSG : CANT_GIGANTAMAX_MSG);
			
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
		
		setMegaMsg(goPokedex, evolTreeGpList, res);
		res.setFinEvo(goPokedex.isFinEvo());

		// スーパーリーグ、ハイパーリーグ制限内最大CP
		setLeagueSafeCp(goPokedex, res, evolTreeList, evolTreeGpList);
	}
	
	/**
	 * 
	 * @param cpTargetGp
	 * @param evolTreeList
	 * @param evolTreeGpList
	 * @param res
	 */
	private void setEgg(
			GoPokedex cpTargetGp, 
			List<Evolution> evolTreeList, 
			List<GoPokedex> evolTreeGpList, 
			AbundanceResponse res) {

		GoPokedex eggGp = cpTargetGp;
		String cpTargetPid = cpTargetGp.getPokedexId();
		// 第一形態のポケモンを取得
		List<String> rootPidList = evolutionProvider.getRoot(cpTargetPid, evolTreeList);
		List<GoPokedex> rootList = rootPidList.stream()
				.map(pid -> {
					return evolTreeGpList.stream()
							.filter(gp -> gp.getPokedexId().equals(pid))
							.findFirst().orElseThrow();
				})
				.toList();
		Optional<GoPokedex> rootOp = rootList.stream()
				.filter(gp -> !gp.getPokedexId().equals(cpTargetPid))
				.findFirst();
		if (rootOp.isPresent()) {
			// ミノムッチの場合、くさきのミノのみ
			eggGp = rootOp.get();
			res.setEggGp(eggGp); // 進化前が存在した場合だけセットする。
		}
		IvRangeCp egg = catchCpUtils.getIvRangeCp(eggGp, new EggsIvRange());
		res.setEgg(new CatchCp(egg, null));
	}
	
	/**
	 * メガシンカ用のメッセージをセットする
	 * 
	 * @param goPokedex
	 * @param evolTreeGpList
	 * @param res
	 */
	private void setMegaMsg(
			GoPokedex goPokedex,
			List<GoPokedex> evolTreeGpList,
			AbundanceResponse res) {

		boolean isMega = PokemonEditUtils.isMega(goPokedex);
		res.setMega(isMega);
		
		if (isMega) {
			// メガシンカの場合は、進化前のポケモン名を取得する。
			String pokeName = evolTreeGpList.stream()
					.filter(gp -> goPokedex.getPreMegaPokedexId().equals(gp.getPokedexId()))
					.map(PokemonEditUtils::appendRemarks)
					.findFirst().orElseThrow();

			res.setCanMega(false);
			res.setMegaMsg(MessageFormat.format(PRE_MEGA_MSG, pokeName));
			return;
		}
		
		// メガシンカ前のポケモンの図鑑IDを取得する。
		List<String> preMegaPidList = evolTreeGpList.stream()
				.filter(gp -> !StringUtils.isEmpty(gp.getPreMegaPokedexId()))
				.map(GoPokedex::getPreMegaPokedexId)
				.sorted()
				.distinct()
				.toList();
		
		String msg;
		boolean canMega = false;
		if (preMegaPidList.isEmpty()) {
			// メガシンカするポケモンが存在しない進化ツリーの場合
			msg = CANT_MEGA_MSG;

		} else if (preMegaPidList.contains(goPokedex.getPokedexId())) {
			// リザードンパターン
			// メガシンカ可能
			canMega = true;
			msg = CAN_MEGA_MSG;
			
		} else {
			// ヒトカゲパターン
			// 〜に進化させれば、メガシンカ可能
			// ラルトスのように複数パターンにメガシンカするパターンも考慮している
			String pokeNames = preMegaPidList.stream()
					.map(pid -> {
						return evolTreeGpList.stream()
								.filter(gp -> gp.getPokedexId().equals(pid))
								.findFirst().orElseThrow();
					})
					.map(PokemonEditUtils::appendRemarks)
					.collect(Collectors.joining(CAN_MEGA_IF_EVOL_MSG_OR_PARTS));
			msg = MessageFormat.format(CAN_MEGA_IF_EVOL_MSG, pokeNames);
		}
			
		res.setCanMega(canMega);
		res.setMegaMsg(msg);
	}
	
	/** 
	 * 制限内最大CP
	 * @param goPokedex
	 * @param res
	 * @param lineageList
	 * @param lineageGpList
	 */
	private void setLeagueSafeCp(
			GoPokedex goPokedex, 
			AbundanceResponse res, 
			List<Evolution> lineageList,
			List<GoPokedex> lineageGpList) {
		
		String pokedexId = goPokedex.getPokedexId();
		// 最終進化のポケモンを取得
		List<String> evolPidList = evolutionProvider.getLeafCanGoEvol(pokedexId, lineageList)
				.stream()
				.filter(pid -> !pokedexId.equals(pid)) // 検索元と検索後のpokedexIdが一致する場合は検索結果なし扱い
				.toList();
		
		if (evolPidList.isEmpty()) {
			// 進化後が存在しない場合
			res.setSuperLeagueSafeCpList(new ArrayList<>());
			res.setHyperLeagueSafeCpList(new ArrayList<>());
			return;
		}

		List<GoPokedex> goPokedexList = evolPidList.stream()
				.map(pid -> {
					return lineageGpList.stream()
							.filter(gp -> gp.getPokedexId().equals(pid))
							.findFirst().orElseThrow();
				})
				.toList();
		
		List<GoPokedexAndCpPl> slLeagueSafeCp = goPokedexList.stream()
				.map(gp -> {
					// 最終進化の最低個体値でスーパーリーグ制限にひっかからないPLを取得し、そのPLから進化前の状態のCPを求める。
					ScpRank slScpRank = scpRankCalculator.createScpRank(gp, 0, 0, 0, scpRankCalculator.SL_CP_LIMIT_PREDICATE);
					String pl = slScpRank.getPl();
					int cp = pokemonGoUtils.calcCp(goPokedex, 0, 0, 0, pl);
					return new GoPokedexAndCpPl(gp, cp, pl);
				})
				.toList();
		List<GoPokedexAndCpPl> hlLeagueSafeCp = goPokedexList.stream()
				.map(gp -> {
					ScpRank hlScpRank = scpRankCalculator.createScpRank(gp, 0, 0, 0, scpRankCalculator.HL_CP_LIMIT_PREDICATE);
					String pl = hlScpRank.getPl();
					int cp = pokemonGoUtils.calcCp(goPokedex, 0, 0, 0, pl);
					return new GoPokedexAndCpPl(gp, cp, pl);
				})
				.toList();
		res.setSuperLeagueSafeCpList(slLeagueSafeCp);
		res.setHyperLeagueSafeCpList(hlLeagueSafeCp);
	}
}
