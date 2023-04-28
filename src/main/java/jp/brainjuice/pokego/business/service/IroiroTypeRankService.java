package jp.brainjuice.pokego.business.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.constant.Type.TypeEffectiveEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.TypeUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IroiroTypeRankElement;
import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;
import jp.brainjuice.pokego.business.service.utils.memory.EvolutionInfo;
import jp.brainjuice.pokego.business.service.utils.memory.TypeChartInfo;
import jp.brainjuice.pokego.web.form.res.IroiroTypeRankResponse;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Service
public class IroiroTypeRankService {

	private TypeChartInfo typeChartInfo;

	private PokedexFilterInfoRepository pokedexFilterInfoRepository;

	private GoPokedexRepository goPokedexRepository;

	private EvolutionInfo evolutionInfo;

	public IroiroTypeRankService(
			TypeChartInfo typeChartInfo,
			PokedexFilterInfoRepository pokedexFilterInfoRepository,
			GoPokedexRepository goPokedexRepository,
			EvolutionInfo evolutionInfo) {
		this.typeChartInfo = typeChartInfo;
		this.pokedexFilterInfoRepository = pokedexFilterInfoRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.evolutionInfo = evolutionInfo;
	}

	/** leastWeaknessで使用するダメージ倍率ごとの重み */
	private final Map<TypeEffectiveEnum, Integer> weightMap = Map.ofEntries(
			Map.entry(TypeEffectiveEnum.MAX, 3), // MAXをHIGH * 2より小さくすることで、HIGH2こよりMAX1この方が順位を高くできる。
			Map.entry(TypeEffectiveEnum.HIGH, 2));

	@AllArgsConstructor
	public enum IroiroTypeRankSearchPattern {
		leastWeakness("弱点が少ない順"),
		qtyOfPoke1Type("よくある組み合わせ順(1タイプ)"),
		qtyOfPoke2Type("よくある組み合わせ順(2タイプ)"),
		strongAtk("こうげき優秀順"),
		strongDef1Only("ぼうぎょ優秀順(1タイプのみ)"),
		strongDef("ぼうぎょ優秀順"),
		;

		/**
		 * 引数に指定された文字列が、IroiroTypeRankSearchPatternに定義されているかを判定する。
		 *
		 * @param type
		 * @return
		 */
		public static boolean isDefined(String type) {
			if (type == null) return false;
			for (IroiroTypeRankSearchPattern te: IroiroTypeRankSearchPattern.values()) {
				if (te.name().equals(type)) {
					return true;
				}
			}
			return false;
		}

		@Getter
		private String jpn;
	}

	public boolean check(String sp, IroiroTypeRankResponse res) {

		// 一旦成功とする。
		res.setSuccess(true);

		if (!IroiroTypeRankSearchPattern.isDefined(sp)) {
			res.setSuccess(false);
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage("存在しない検索パターンが指定されました。");
		}

		return res.isSuccess();
	}

	public void exec(IroiroTypeRankSearchPattern searchPattern, IroiroTypeRankResponse res) {

		res.setSearchPattern(searchPattern.getJpn());

		List<IroiroTypeRankElement> itreList = switch (searchPattern) {
		case leastWeakness -> getLeastWeaknessRank("×{0}倍の弱点：{1}");
		case qtyOfPoke1Type -> getQuantityOfPokemon1Type("種族数：{0}", "ポケモン数：{0}", "対象ポケモン：{0}");
		case qtyOfPoke2Type -> getQuantityOfPokemon2Type("種族数：{0}", "ポケモン数：{0}", "対象ポケモン：{0}");
		case strongAtk -> getStrongAtkRank("×{0}倍：{1}");
		case strongDef1Only -> getStrongDefOnly1Rank("×{0}倍：{1}");
		case strongDef -> getStrongDefRank("×{0}倍：{1}");
		};

		res.setTypeRankList(itreList);

		res.setMsgsHeader(switch (searchPattern) {
		case leastWeakness -> "弱点タイプ";
		case qtyOfPoke1Type -> "詳細";
		case qtyOfPoke2Type -> "詳細";
		case strongAtk -> "詳細";
		case strongDef1Only -> "詳細";
		case strongDef -> "詳細";
		});

		res.setMsgDecoration(switch (searchPattern) {
		case leastWeakness -> true;
		case qtyOfPoke1Type -> false;
		case qtyOfPoke2Type -> false;
		case strongAtk -> true;
		case strongDef1Only -> true;
		case strongDef -> true;
		});
	}

	/**
	 * @param msgFormat
	 * @return
	 */
	private List<IroiroTypeRankElement> getLeastWeaknessRank(String msgFormat) {

		// タイプごとに弱点タイプを保持したマップを作成する。
		Map<TwoTypeKey, Map<TypeEffectiveEnum, List<TypeEnum>>> weakEffMap = TypeUtils.getTwoTypeStreamContainsOneType()
				.map(ttk -> Map.entry(
						ttk,
						typeChartInfo.getDefenderTypes(ttk.getType1(),ttk.getType2()) // Map<各倍率: List<タイプ>>を取得
						.entrySet().stream()
						// 弱点だけに絞り込む。
						.filter(effEntry -> TypeEffectiveEnum.MAX == effEntry.getKey() || TypeEffectiveEnum.HIGH == effEntry.getKey())
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// スコアをValueにもつ、降順にソートされたマップ
		Map<TwoTypeKey, Integer> sortedScoreMap = weakEffMap.entrySet().stream()
				.map(entry -> {
					int score = 0;
					for (Map.Entry<TypeEffectiveEnum, List<TypeEnum>> effEntry: entry.getValue().entrySet()) {
						// タイプ数×重み
						score+=effEntry.getValue().size() * weightMap.get(effEntry.getKey());
					}
					return Map.entry(entry.getKey(), score);
				})
				.sorted((o1, o2) -> o1.getValue().intValue() - o2.getValue().intValue())
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(a, b) -> a,
						LinkedHashMap::new)); // 順番を担保

		// メッセージリストを生成する。
		Map<TwoTypeKey, List<String>> msgsMap = weakEffMap.entrySet().stream()
				.collect(Collectors.toMap(
						entry -> entry.getKey(),
						entry -> {
							List<String> msgList = new ArrayList<>();
							entry.getValue().forEach((k, v) -> {
								if (!v.isEmpty()) {
									msgList.add(MessageFormat.format(
											msgFormat,
											k.getDamageMultiplier(),
											TypeUtils.joinType(v, TypeEnum::getJpn, ", ")));
								}
							});
							return msgList;
						}));

		// 返却値の作成
		List<IroiroTypeRankElement> itreList = new ArrayList<>();
		{
			int rank = 0;
			int tmpScore = 0;
			for (Map.Entry<TwoTypeKey, Integer> scoreEntry: sortedScoreMap.entrySet()) {

				int score = scoreEntry.getValue().intValue();
				// scoreが異なる場合、ランクを1インクリメントさせる。
				if (score > tmpScore) {
					rank+=1;
				}

				itreList.add(new IroiroTypeRankElement(
						scoreEntry.getKey(),
						rank,
						msgsMap.get(scoreEntry.getKey())));

				tmpScore = score;
			}
		}

		return itreList;
	}

	/**
	 * @param msgRaceQty
	 * @param msgPokeQty
	 * @param msgPokes
	 * @return
	 */
	private List<IroiroTypeRankElement> getQuantityOfPokemon1Type(String msgRaceQty, String msgPokeQty, String msgPokes) {
		return getQuantityOfPokemon(msgRaceQty, msgPokeQty, msgPokes, TypeUtils.getOneTypeTwoTypeStream());
	}

	/**
	 * @param msgRaceQty
	 * @param msgPokeQty
	 * @param msgPokes
	 * @return
	 */
	private List<IroiroTypeRankElement> getQuantityOfPokemon2Type(String msgRaceQty, String msgPokeQty, String msgPokes) {
		return getQuantityOfPokemon(msgRaceQty, msgPokeQty, msgPokes, TypeUtils.getTwoTypeStream());
	}

	/**
	 * ポケモンの数（よくある組み合わせ順）のメッセージをリストで取得する。
	 *
	 * @param msgRaceQty
	 * @param msgPokeQty
	 * @param msgPokes
	 * @param ttkStream
	 * @return
	 */
	private List<IroiroTypeRankElement> getQuantityOfPokemon(
			String msgRaceQty, String msgPokeQty, String msgPokes, Stream<TwoTypeKey> ttkStream) {

		// key: TwoTypeKey, value: Map<PokedexNo, List<PokedexId>>>のMapを作成する。
		Map<TwoTypeKey, Map<Integer, List<String>>> basePdxNoMap = ttkStream
				.map(ttk -> Map.entry(ttk, pokedexFilterInfoRepository.findIdByType(ttk))) // Map<TwoTypeKey, List<PokedexId>
				.map(entry -> Map.entry(
						entry.getKey(),
						entry.getValue().stream()
						.collect(Collectors.groupingBy(pid -> evolutionInfo.basePokedexNo(pid))))) // 同系統のポケモンごとに括る。
				.sorted((o1, o2) -> o2.getValue().size() - o1.getValue().size()) // 降順に並び替える。
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(a, b) -> a,
						LinkedHashMap::new)); // 順番を担保

		// 返却値の作成
		List<IroiroTypeRankElement> itreList = new ArrayList<>();
		{
			int rank = 1;
			int tmpSize = 0;
			for (Map.Entry<TwoTypeKey, Map<Integer, List<String>>> entry: basePdxNoMap.entrySet()) {

				int size = entry.getValue().size();
				// 種族数が異なる場合、ランクを1インクリメントさせる。
				if (size < tmpSize) {
					rank+=1;
				}

				itreList.add(new IroiroTypeRankElement(
						entry.getKey(),
						rank,
						getMsgsQtyOfPokemon(entry, msgRaceQty, msgPokeQty, msgPokes)));

				tmpSize = size;
			}
		}
		return itreList;

	}

	/**
	 * ポケモンの数（よくある組み合わせ順）のメッセージをリストで取得する。
	 *
	 * @param entry
	 * @param msgRaceQty
	 * @param msgPokeQty
	 * @param msgPokes
	 * @return
	 */
	private List<String> getMsgsQtyOfPokemon(
			Map.Entry<TwoTypeKey, Map<Integer, List<String>>> entry,
			String msgRaceQty,
			String msgPokeQty,
			String msgPokes) {

		List<String> pokeList = entry.getValue().entrySet().stream()
				.flatMap(entry2 -> entry2.getValue().stream()) // 種族ごとにまとまってる図鑑IDを直列化する。
				.map(pid -> goPokedexRepository.findById(pid).get())
				.sorted(PokemonEditUtils.getPokedexComparator(1)) // 図鑑IDの昇順
				.map(PokemonEditUtils::appendRemarks) // ポケモン名 + "(" + 備考 + ")"
				.collect(Collectors.toList());

		// メッセージリスト作成
		List<String> msgList = new ArrayList<>();
		msgList.add(MessageFormat.format(msgRaceQty, entry.getValue().size()));
		msgList.add(MessageFormat.format(msgPokeQty, pokeList.size()));
		if (!pokeList.isEmpty()) {
			msgList.add(MessageFormat.format(msgPokes, StringUtils.join(pokeList, ", ")));
		}

		return msgList;
	}

	private List<IroiroTypeRankElement> getStrongAtkRank(String msgFormat) {

		return getStrongTypeRank(
				msgFormat,
				TypeUtils.getOneTypeTwoTypeStream(),
				ttk -> typeChartInfo.attackerScore(ttk.getType1()),
				entry -> {
					return TypeEffectiveEnum.MAX != entry.getKey()
							&& TypeEffectiveEnum.MIN != entry.getKey()
							&& TypeEffectiveEnum.NORMAL != entry.getKey();
				},
				ttk -> typeChartInfo.getAttackerTypes(ttk.getType1()));
	}

	private List<IroiroTypeRankElement> getStrongDefOnly1Rank(String msgFormat) {

		return getStrongTypeRank(
				msgFormat,
				TypeUtils.getOneTypeTwoTypeStream(),
				ttk -> typeChartInfo.defenderScore(ttk.getType1()),
				entry -> {
					return TypeEffectiveEnum.MAX != entry.getKey()
							&& TypeEffectiveEnum.MIN != entry.getKey()
							&& TypeEffectiveEnum.NORMAL != entry.getKey();
				},
				ttk -> typeChartInfo.getDefenderTypes(ttk.getType1()));
	}

	private List<IroiroTypeRankElement> getStrongDefRank(String msgFormat) {

		return getStrongTypeRank(
				msgFormat,
				TypeUtils.getTwoTypeStreamContainsOneType(),
				ttk -> typeChartInfo.defenderScore(ttk.getType1(), ttk.getType2()),
				entry -> TypeEffectiveEnum.NORMAL != entry.getKey(),
				ttk -> typeChartInfo.getDefenderTypes(ttk.getType1(), ttk.getType2()));
	}

	private List<IroiroTypeRankElement> getStrongTypeRank(
			String msgFormat,
			Stream<TwoTypeKey> twoTypeKeyStream,
			Function<TwoTypeKey, Double> calcScoreFunc,
			Predicate<Map.Entry<TypeEffectiveEnum, List<TypeEnum>>> effPredicate,
			Function<TwoTypeKey, Map<TypeEffectiveEnum, List<TypeEnum>>> getTypeEffFunc) {

		Map<TwoTypeKey, Double> sortedScoreMap = twoTypeKeyStream
				.map(ttk -> Map.entry(ttk, Double.valueOf(calcScoreFunc.apply(ttk))))
				.sorted((o1, o2) -> o2.getValue().doubleValue() - o1.getValue().doubleValue() > 0 ? 1 : -1) // 降順で並び替える
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

		// 返却値の作成
		List<IroiroTypeRankElement> itreList = new ArrayList<>();
		{
			int rank = 1;
			double tmpScore = 0;
			for (Map.Entry<TwoTypeKey, Double> scoreEntry: sortedScoreMap.entrySet()) {

				double score = scoreEntry.getValue();
				// 種族数が異なる場合、ランクを1インクリメントさせる。
				if (score < tmpScore) {
					rank+=1;
				}

				List<String> msgList = getTypeEffFunc.apply(scoreEntry.getKey())
						.entrySet().stream()
						.filter(effPredicate)
						.map(entry -> {
							String types = entry.getValue().isEmpty() ? "なし" : StringUtils.join(
									entry.getValue().stream().map(TypeEnum::getJpn).collect(Collectors.toList()),
									", ");
							return MessageFormat.format(
									msgFormat,
									entry.getKey().getDamageMultiplier(),
									types);
						})
						.collect(Collectors.toList());

				itreList.add(new IroiroTypeRankElement(
						scoreEntry.getKey(),
						rank,
						msgList));

				tmpScore = score;
			}
		}
		return itreList;
	}

}
