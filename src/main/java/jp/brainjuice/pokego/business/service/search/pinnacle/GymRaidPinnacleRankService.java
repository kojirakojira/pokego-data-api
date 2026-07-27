package jp.brainjuice.pokego.business.service.search.pinnacle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jp.brainjuice.pokego.business.constant.LearningPatternEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.constant.WeatherBoosts;
import jp.brainjuice.pokego.business.constant.WeatherBoosts.WeatherEnum;
import jp.brainjuice.pokego.business.service.search.utils.GymRaidDamageCalculator;
import jp.brainjuice.pokego.business.service.search.utils.MovesUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.damage.GymRaidAttackScoreInDto;
import jp.brainjuice.pokego.business.service.search.utils.dto.damage.GymRaidAttackScoreOutDto;
import jp.brainjuice.pokego.business.service.search.utils.dto.pinnacle.GymRaidPinnacleRankTempDto;
import jp.brainjuice.pokego.business.service.search.utils.dto.pinnacle.PokemonAttackCombination;
import jp.brainjuice.pokego.business.service.search.utils.dto.type.TwoTypeKey;
import jp.brainjuice.pokego.cache.inmemory.TypeCommentMap;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonChargedAttackRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonFastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonFastAttack;
import jp.brainjuice.pokego.web.search.form.req.pinnacle.GymRaidPinnacleRankRequest;
import jp.brainjuice.pokego.web.search.form.res.pinnacle.GymRaidPinnacleRankResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GymRaidPinnacleRankService {

	private GoPokedexRepository goPokedexRepository;

	private PokemonFastAttackRepository pokemonFastAttackRepository;

	private PokemonChargedAttackRepository pokemonChargedAttackRepository;

	private GymRaidDamageCalculator gymRaidDamageCalculator;

	private TypeCommentMap typeCommentMap;

	private WeatherBoosts weatherBoosts;

	private MovesUtils movesUtils;

	public enum SelectPattern {
		/** 含める */
		include,
		/** 除く */
		except,
		/** だけにする */
		only
	}

	public enum Order {
		/** 最強（降順） */
		desc,
		/** 最弱（昇順） */
		asc
	}

	public GymRaidPinnacleRankService(
			GoPokedexRepository goPokedexRepository,
			PokemonFastAttackRepository pokemonFastAttackRepository,
			PokemonChargedAttackRepository pokemonChargedAttackRepository,
			GymRaidDamageCalculator gymRaidDamageCalculator,
			TypeCommentMap typeCommentMap,
			WeatherBoosts weatherBoosts,
			MovesUtils movesUtils) {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonFastAttackRepository = pokemonFastAttackRepository;
		this.pokemonChargedAttackRepository = pokemonChargedAttackRepository;
		this.gymRaidDamageCalculator = gymRaidDamageCalculator;
		this.typeCommentMap = typeCommentMap;
		this.weatherBoosts = weatherBoosts;
		this.movesUtils = movesUtils;
	}

	public void exec(GymRaidPinnacleRankRequest req, GymRaidPinnacleRankResponse res) {

		TypeEnum oppType1 = req.getOppType1();
		TypeEnum oppType2 = req.getOppType2();
		List<TypeEnum> ownTypeList = req.getOwnTypes();
		TwoTypeKey defenderType = new TwoTypeKey(oppType1, oppType2);
		WeatherEnum weather = StringUtils.isEmpty(req.getWeather()) ? null : WeatherEnum.valueOf(req.getWeather());
		SelectPattern megaSelected = req.getMegaSelected();
		SelectPattern shadowSelected = req.getShadowSelected();
		Order order = req.getOrder();
		boolean isUnique = req.isUnique();

		// 並び順を指定する。
		Comparator<GymRaidPinnacleRankTempDto> comparator = switch (order) {
		case desc -> (o1, o2) -> Double.compare(o2.getAttackScore(), o1.getAttackScore());
		case asc -> (o1, o2) -> Double.compare(o1.getAttackScore(), o2.getAttackScore());
		};

		List<GymRaidPinnacleRankTempDto> tempDtoList = createTmpDtoList(megaSelected, shadowSelected, ownTypeList);

		GymRaidAttackScoreInDto inDto = new GymRaidAttackScoreInDto(); // オブジェクトを使い回す
		Stream<GymRaidPinnacleRankTempDto> combiStream = tempDtoList.stream()
				.map(tempDto -> {
					GoPokedex gp = tempDto.getGoPokedex();
					inDto.setGoPokedex(gp);
					inDto.setFastAttack(tempDto.getPfa().getFastAttack());
					inDto.setChargedAttack(tempDto.getPca().getChargedAttack());
					inDto.setDefenderType(defenderType);
					inDto.setWeather(weather);
					inDto.setShadow(tempDto.isShadow());
					GymRaidAttackScoreOutDto outDto = gymRaidDamageCalculator.attackScore(inDto);
					tempDto.setFastAttackScore(outDto.getFastAttackScore());
					tempDto.setChargedAttackScore(outDto.getChargedAttackScore());
					tempDto.setAttackScore(outDto.getAttackScore());
					return tempDto;
				})
				.sorted(comparator);

		if (isUnique) {
			// ポケモンの重複を除去する。並び順が若い方を優先する。
			combiStream = combiStream
					.collect(Collectors.toMap(
							// pokedexIdの後ろにシャドウか否かに応じた1,0を付与することにより、シャドウと通常の両方の最強の技構成を残す
							pac -> pac.getGoPokedex().getPokedexId() + (pac.isShadow() ? '1' : '0'),
							Function.identity(),
							(o, n) -> o, // old優先
							LinkedHashMap::new))
					.entrySet().stream()
					.map(Map.Entry::getValue);
		}
		// 上限1000で切り出し、表示用の型に変換する。
		List<PokemonAttackCombination> combiList = combiStream
				.limit(1000L)
				.map(this::convPokemonAttackCombination)
				.toList();

		res.setCombiList(combiList);

		res.setTypeComments(typeCommentMap.get(oppType1, oppType2));
		if (weather != null) {
			List<TypeEnum> wbTypeList = weatherBoosts.getTypeWbLookupMap().get(weather);
			res.setWbTypeList(wbTypeList);
		}
	}

	/**
	 * 通常ポケモン、リトレーン後のポケモンのPokemonAttackCombinationのリストを作成する。<br>
	 * FastAttackとChargedAttackは設定しないため、作成途中のリストを返却する。
	 *
	 * @param megaSelected
	 * @param shadowSelected
	 * @param ownTypeList
	 * @return
	 */
	private List<GymRaidPinnacleRankTempDto> createTmpDtoList(
			SelectPattern megaSelected,
			SelectPattern shadowSelected,
			List<TypeEnum> ownTypeList) {

		boolean ownFilterFlg = !CollectionUtils.isEmpty(ownTypeList);
		// 攻撃する側のポケモンの一覧（実装済みの全ポケモン）を取得する
		List<GoPokedex> goPokedexList = goPokedexRepository.findByImplFlg(true).stream()
				.filter(gp -> !ownFilterFlg || ownTypeList.contains(gp.getType1()) || ownTypeList.contains(gp.getType2())) // 自分のポケモンをタイプで絞り込む
				.toList();

		// ポケモンが覚える技をすべて取得する
		// 通常技
		Map<String, List<PokemonFastAttack>> pfaMap = movesUtils.convHiddenPowerForPokemonFastAttackList( // めざめるパワーを変換
				pokemonFastAttackRepository.findAllJoinFastAttack()
				).stream()
				.filter(pfa -> !MovesUtils.TRANSFORM_MOVE_ID.equals(pfa.getMoveId())) // へんしんを排除する。
				.collect(Collectors.groupingBy(PokemonFastAttack::getPokedexId));
		List<PokemonChargedAttack> pcaList = pokemonChargedAttackRepository.findAllJoinChargedAttack();
		// スペシャル技
		Map<String, List<PokemonChargedAttack>> pcaMap = pcaList.stream()
				.collect(Collectors.groupingBy(PokemonChargedAttack::getPokedexId));

		Predicate<GoPokedex> gpPredicate = switch (megaSelected) {
		case include -> (gp) -> true;
		case except -> (gp) -> gp.getPreMegaPokedexId() == null;
		case only -> (gp) -> gp.getPreMegaPokedexId() != null;
		};
		if (shadowSelected == SelectPattern.only) {
			// シャドウだけの場合はすべてfalse
			gpPredicate = (gp) -> false;
		}
		/*
		 * 通常状態におけるポケモン×通常技×スペシャル技の全パターン
		 */
		// 通常技のCombinationのStreamを作成
		Stream<GymRaidPinnacleRankTempDto> combiList = goPokedexList.stream()
				.filter(gpPredicate)
				.flatMap(gp -> {
					String pokedexId = gp.getPokedexId();
					if (gp.getPreMegaPokedexId() != null) {
						// メガシンカの場合、メガシンカ前の技を適用する。
						pokedexId = gp.getPreMegaPokedexId();
					}
					List<PokemonFastAttack> targetGpPfaList = pfaMap.get(pokedexId);
					List<PokemonChargedAttack> targetGpPcaList = pcaMap.get(pokedexId);

					if (targetGpPfaList == null || targetGpPcaList == null) {
						log.debug("通常技、スペシャル技のどちらかが存在しませんでした。（pokedexId: {}）", pokedexId);
						return Stream.empty();
					}
					List<GymRaidPinnacleRankTempDto> pacList = createOneTempDtoList(gp, targetGpPfaList, targetGpPcaList,
							false);
					return pacList.stream();
				});

		/*
		 * シャドウポケモンにおけるポケモン×通常技×スペシャル技
		 */
		Predicate<PokemonChargedAttack> shadowPredicate = switch (shadowSelected) {
		case include -> (pca) -> LearningPatternEnum.shadow == pca.getLearningPattern();
		case except -> (pca) -> false;
		case only -> (pca) -> LearningPatternEnum.shadow == pca.getLearningPattern();
		};
		if (megaSelected == SelectPattern.only) {
			// メガシンカだけの場合はすべてfalse
			shadowPredicate = (pca) -> false;
		}
		// シャドウポケモンの一覧を作成。
		// ※pokemon_charged_attackテーブルにはメガ進化後のポケモンの情報は追加していないため、シャドウメガ〇〇になることはない。
		List<GoPokedex> goPokedexShadowList = pcaList.stream()
				.filter(shadowPredicate)
				.map(pca -> goPokedexList.stream()
						.filter(gp -> pca.getPokedexId().equals(gp.getPokedexId()))
						.findAny().orElse(null)) // ポケモンGO未実装のポケモンがいた場合はnullになる。
				.filter(gp -> gp != null) // nullになった要素を除去する。
				.toList();
		// シャドウポケモンのCombinationのリストを作成
		Stream<GymRaidPinnacleRankTempDto> shadowCombiList = goPokedexShadowList.stream()
				.flatMap(gp -> {
					String pokedexId = gp.getPokedexId();
					List<PokemonFastAttack> targetGpPfaList = pfaMap.get(pokedexId);
					List<PokemonChargedAttack> targetGpPcaList = pcaMap.get(pokedexId);
					if (targetGpPfaList == null || targetGpPcaList == null) {
						log.warn("{}でエラー", pokedexId);
						return Stream.empty();
					}
					List<GymRaidPinnacleRankTempDto> pacList = createOneTempDtoList(gp, targetGpPfaList, targetGpPcaList,
							true);
					return pacList.stream();
				});

		return Stream.concat(combiList, shadowCombiList).toList();
	}

	private List<GymRaidPinnacleRankTempDto> createOneTempDtoList(
			GoPokedex goPokedex,
			List<PokemonFastAttack> targetGpPfaList,
			List<PokemonChargedAttack> targetGpPcaList,
			boolean shadowFlg) {

		// そのポケモンの通常技 * スペシャル技の数を初期容量としてリストを生成する。
		List<GymRaidPinnacleRankTempDto> combiList = new ArrayList<>(targetGpPfaList.size() * targetGpPcaList.size());
		for (PokemonFastAttack pfa : targetGpPfaList) {
			for (PokemonChargedAttack pca : targetGpPcaList) {
				if ((shadowFlg && pca.getLearningPattern() == LearningPatternEnum.purified)
						|| (!shadowFlg && pca.getLearningPattern() == LearningPatternEnum.shadow)) {
					// シャドウでリトレーン後の技、またはリトレーン後でシャドウの技の場合はスキップ
					continue;
				}
				GymRaidPinnacleRankTempDto combi = new GymRaidPinnacleRankTempDto();
				combi.setGoPokedex(goPokedex);
				combi.setPfa(pfa);
				combi.setPca(pca);
				combi.setShadow(shadowFlg);
				combiList.add(combi);
			}
		}
		return combiList;
	}

	private PokemonAttackCombination convPokemonAttackCombination(GymRaidPinnacleRankTempDto tempDto) {

		PokemonAttackCombination combi = new PokemonAttackCombination();
		GoPokedex gp = tempDto.getGoPokedex();
		PokemonFastAttack pfa = tempDto.getPfa();
		PokemonChargedAttack pca = tempDto.getPca();
		combi.setGoPokedex(gp);
		combi.setFaMoveId(pfa.getMoveId());
		combi.setCaMoveId(pca.getMoveId());
		combi.setFastAttackLearningPattern(pfa.getLearningPattern());
		combi.setChargedAttackLearningPattern(pca.getLearningPattern());
		combi.setMega(gp.getPreMegaPokedexId() != null);
		combi.setShadow(tempDto.isShadow());

		combi.setFaAttackScore(tempDto.getFastAttackScore());
		combi.setCaAttackScore(tempDto.getChargedAttackScore());
		combi.setAttackScore(tempDto.getAttackScore());
		FastAttack fa = pfa.getFastAttack();
		combi.setFaName(fa.getName());
		combi.setFaType(fa.getType());
		ChargedAttack ca = pca.getChargedAttack();
		combi.setCaName(ca.getName());
		combi.setCaType(ca.getType());

		String attribute = tempDto.isShadow() ? "シャドウ" : "";
		attribute = gp.getPreMegaPokedexId() != null ? "メガ" : attribute;
		combi.setAttribute(attribute);
		return combi;
	}
}
