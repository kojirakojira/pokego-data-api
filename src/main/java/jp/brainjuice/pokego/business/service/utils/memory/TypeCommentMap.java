package jp.brainjuice.pokego.business.service.utils.memory;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.constant.Type.TypeEffectiveEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.TypeUtils;
import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;
import jp.brainjuice.pokego.business.service.utils.memory.evo.EvolutionProvider;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;

/**
 * タイプごとのメッセージを保持します。
 * TwoTypeKeyをキーとして、メッセージをリストで保持します。
 *
 * @author saibabanagchampa
 *
 */
@Component
public class TypeCommentMap extends HashMap<TwoTypeKey, LinkedHashSet<String>> {

	@Autowired
	public TypeCommentMap(
			TypeChartInfo typeChartInfo,
			GoPokedexRepository goPokedexRepository,
			EvolutionProvider evolutionProvider) throws PokemonDataInitException {

		// メッセージ作成
		createMessageMap(typeChartInfo, goPokedexRepository, evolutionProvider);
	}

	public LinkedHashSet<String> get(TypeEnum te1, TypeEnum te2) {
		return get(new TwoTypeKey(te1, te2));
	}

	/**
	 * メッセージを作成してmsgMapにputする。
	 *
	 * @param typeChartInfo
	 * @param goPokedexRepository
	 * @param evolutionProvider
	 * @throws PokemonDataInitException
	 */
	public void createMessageMap(
			TypeChartInfo typeChartInfo,
			GoPokedexRepository goPokedexRepository,
			EvolutionProvider evolutionProvider) throws PokemonDataInitException {

		// 1種族しかない組み合わせ
		onlyTwoType(goPokedexRepository, evolutionProvider, "{0}, {1}は1種族しか存在しない組み合わせです。（対象ポケモン：{2}）");

		// 最も定番の組み合わせ
		mostPopularTwoType(goPokedexRepository, evolutionProvider, "{0}, {1}は最も定番の組み合わせで、{2}体のポケモンが該当します。（対象ポケモン：{3}）");

		// 存在しない組み合わせ
		notExistsTwoType(goPokedexRepository, "{0}, {1}は、現在のポケモンにおいて存在しない組み合わせです。");

		// こうげき、ぼうぎょ時のスコア
		maxScore(typeChartInfo, "こうげき時の評価が最高評価です！", () -> getAttackerScoreMap(typeChartInfo));
		minScore(typeChartInfo, "こうげき時の評価が最低評価です…。", () -> getAttackerScoreMap(typeChartInfo));
		maxScore(typeChartInfo, "ぼうぎょ時の評価が最高評価です！", () -> getDefenderScoreMap(typeChartInfo));
		minScore(typeChartInfo, "ぼうぎょ時の評価が最低評価です…。", () -> getDefenderScoreMap(typeChartInfo));

		// 弱点が少ないタイプの組み合わせ
		leastWeaknessType(typeChartInfo, "{0}は、弱点タイプが最も少なく{1}しかありません。弱点タイプが{1}の組み合わせは、全タイプ中{2}存在します。（弱点タイプ：{3}）");

		// めっぽう弱い
		weak(typeChartInfo, "{0}のこうげきに対してめっぽう弱いです…。(×{1}倍)", TypeEffectiveEnum.MAX);
		// とてつもない耐性
		weak(typeChartInfo, "{0}のこうげきに対してとてつもなく耐性があります。(×{1}倍)", TypeEffectiveEnum.MIN);
		// 強い耐性
		weak(typeChartInfo, "{0}のこうげきに対して強い耐性があります。(×{1}倍)", TypeEffectiveEnum.VERY_LOW);
		// 唯一の×2.56
		onlyOneType(typeChartInfo, "{0}のこうげきに対して唯一×{1}倍のダメージ倍率が出ます。", TypeEffectiveEnum.MAX);
		// 唯一の×0.244140625
		onlyOneType(typeChartInfo, "{0}のこうげきに対して唯一×{1}倍のダメージ倍率が出ます。", TypeEffectiveEnum.MIN);
		// 唯一の×0.390625
		onlyOneType(typeChartInfo, "{0}のこうげきに対して唯一×{1}倍のダメージ倍率が出ます。", TypeEffectiveEnum.VERY_LOW);
		// すべてのダメージ倍率
		allDamageMultiplier(typeChartInfo, "{0}, {1}は、全ての倍率でこうげきを受けうるタイプの組み合わせです。これは、全組み合わせ中{2}タイプのみです。");

		// ダメージ倍率数の最大（こうげき）
		{
			String strengthMsgFormat = "{0}は、こうげき時に×{1}倍のダメージ倍率が出るタイプが{2}あります。これは全タイプ中最も多く、こうげき面で優秀です。（対象タイプ：{3}）";
			String weakMsgFormat = "{0}は、こうげき時に×{1}倍のダメージ倍率が出るタイプが{2}あります。これは全タイプ中最も多く、こうげき面に難ありです…。（対象タイプ：{3}）";
			maxCountAttackerDamageMult(
					typeChartInfo,
					strengthMsgFormat,
					TypeEffectiveEnum.HIGH);

			maxCountAttackerDamageMult(
					typeChartInfo,
					weakMsgFormat,
					TypeEffectiveEnum.LOW);
		}

		// ダメージ倍率数の最大（ぼうぎょ）
		{
			String weakMsgFormat = "{0}は、ぼうぎょ時に×{1}倍のダメージ倍率が出るタイプが{2}あります。これは全タイプ中最も多く、ぼうぎょ面に難ありです…。（対象タイプ：{3}）";
			String strengthMsgFormat = "{0}は、ぼうぎょ時に×{1}倍のダメージ倍率が出るタイプが{2}あります。これは全タイプ中最も多く、ぼうぎょ面で優秀です。（対象タイプ：{3}）";
			maxCountDefenderDamageMult(
					typeChartInfo,
					weakMsgFormat + "",
					TypeEffectiveEnum.MAX);
			maxCountDefenderDamageMult(
					typeChartInfo,
					weakMsgFormat,
					TypeEffectiveEnum.HIGH);
			maxCountDefenderDamageMult(
					typeChartInfo,
					strengthMsgFormat,
					TypeEffectiveEnum.LOW);
			maxCountDefenderDamageMult(
					typeChartInfo,
					strengthMsgFormat,
					TypeEffectiveEnum.VERY_LOW);
			maxCountDefenderDamageMult(
					typeChartInfo,
					strengthMsgFormat,
					TypeEffectiveEnum.MIN);
		}

	}

	private void maxScore(TypeChartInfo typeChartInfo, String msg, Supplier<Map<TwoTypeKey, Double>> sup) {

		Map<TwoTypeKey, Double> scoreMap = sup.get();

		double max = scoreMap.entrySet().stream()
				.map(Map.Entry::getValue)
				.max((o1, o2) -> o1 - o2 > 0 ? 1 : -1)
				.get();

		scoreMap.entrySet().stream()
		.filter(entry -> entry.getValue() >= max)
		.map(Map.Entry::getKey)
		.forEach(ttk -> putMsg(ttk, msg));
	}

	private void minScore(TypeChartInfo typeChartInfo, String msg, Supplier<Map<TwoTypeKey, Double>> sup) {

		Map<TwoTypeKey, Double> scoreMap = sup.get();

		double min = scoreMap.entrySet().stream()
				.map(Map.Entry::getValue)
				.min((o1, o2) -> o1 - o2 > 0 ? 1 : -1)
				.get();

		scoreMap.entrySet().stream()
		.filter(entry -> entry.getValue() <= min)
		.map(Map.Entry::getKey)
		.forEach(ttk -> putMsg(ttk, msg));
	}

	/**
	 * @param typeChartInfo
	 * @return
	 */
	private Map<TwoTypeKey, Double> getAttackerScoreMap(TypeChartInfo typeChartInfo) {

		return TypeUtils.getTwoTypeStreamContainsOneType()
				.collect(Collectors.toMap(
						ttk -> ttk,
						ttk -> {
							double score = typeChartInfo.attackerScore(ttk.getType1());
							if (ttk.getType2() != null) {
								score += typeChartInfo.attackerScore(ttk.getType2());
								score /= 2;
							}
							return Double.valueOf(score);
						}));
	}

	/**
	 * @param typeChartInfo
	 * @return
	 */
	private Map<TwoTypeKey, Double> getDefenderScoreMap(TypeChartInfo typeChartInfo) {

		return TypeUtils.getTwoTypeStreamContainsOneType()
				.collect(Collectors.toMap(
						ttk -> ttk,
						ttk -> {
							double score = typeChartInfo.defenderScore(ttk.getType1(), ttk.getType2());
							return Double.valueOf(score);
						}));
	}

	/**
	 * 特定のタイプから、その倍率のダメージ倍率になるタイプを取得し、その旨を示すメッセージを作成します。
	 *
	 * @param typeChartInfo
	 * @param msgFormat
	 * @param typeEff
	 */
	private void weak(TypeChartInfo typeChartInfo, String msgFormat, TypeEffectiveEnum typeEff) {

		// 2タイプに対して、引数に指定されたダメージ倍率になるタイプのリストを取得する。
		Map<TwoTypeKey, List<String>> weakTypeMap = TypeUtils.getTwoTypeStream()
				.collect(Collectors.toMap(
						ttk -> ttk,
						ttk -> typeChartInfo.getDefenderTypes(ttk.getType1(), ttk.getType2()).get(typeEff)))
				.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(Collectors.toMap(
						entry -> entry.getKey(),
						entry -> entry.getValue().stream().map(TypeEnum::getJpn).collect(Collectors.toList())));

		weakTypeMap.forEach((k, v) -> {
			putMsg(k, MessageFormat.format(msgFormat, StringUtils.join(v, ", "), typeEff.getDamageMultiplier()));
		});

	}

	/**
	 * 特定のタイプから、唯一その倍率のダメージ倍率になるタイプを取得し、その旨を示すメッセージを作成します。
	 *
	 * @param typeChartInfo
	 * @param msgFormat
	 * @param typeEff
	 */
	private void onlyOneType(TypeChartInfo typeChartInfo, String msgFormat, TypeEffectiveEnum typeEff) {

		// Map<2タイプ, 引数に指定されたダメージ倍率になるタイプのリスト>
		Map<TwoTypeKey, List<TypeEnum>> typeListMap = TypeUtils.getTwoTypeStream()
				.collect(Collectors.toMap(
						ttk -> ttk,
						ttk -> typeChartInfo.getDefenderTypes(ttk.getType1(), ttk.getType2()).get(typeEff)))
				.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue));

		// そのダメージ倍率になるタイプの内、1つしか存在しないタイプのリスト
		List<TypeEnum> onlyTypeList = typeListMap.entrySet().stream()
				.flatMap(entry -> entry.getValue().stream()) // Valueだけを抽出し、直列化する。
				.collect(Collectors.groupingBy(te -> te, Collectors.counting())) // タイプごとの出現件数を求める。(Map<TypeEnum, Long>)
				.entrySet().stream()
				.filter(entry -> entry.getValue().equals(1L)) // 1件だけのタイプに絞り込む
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		// Map<2タイプ, 引数に指定されたダメージ倍率の内の、唯一のタイプ>
		Map<TwoTypeKey, TypeEnum> twoTypeMap = typeListMap.entrySet().stream()
				.filter(entry -> entry.getValue().stream()
						.anyMatch(te -> onlyTypeList.contains(te))) // 1つしか存在しないタイプが含まれているEntryに絞り込む。
				.collect(Collectors.toMap(
						entry -> entry.getKey(),
						entry -> {
							return entry.getValue().stream()
									.filter(te -> onlyTypeList.contains(te))
									.findFirst().get(); // 1つしか存在しないタイプを抽出する。
						}));

		twoTypeMap.forEach((k, v) -> {
			// put
			putMsg(k, MessageFormat.format(msgFormat, v.getJpn(), typeEff.getDamageMultiplier()));
		});
	}

	/**
	 * すべてのダメージ倍率を網羅している旨を伝えるメッセージを作成する。
	 *
	 * @param typeChartInfo
	 * @param msgFormat
	 */
	private void allDamageMultiplier(TypeChartInfo typeChartInfo, String msgFormat) {

		List<TwoTypeKey> twoTypeKeyList = TypeUtils.getTwoTypeStream()
				.collect(Collectors.toMap(
						ttk -> ttk,
						ttk -> typeChartInfo.getDefenderTypes(ttk.getType1(), ttk.getType2())))
				.entrySet().stream()
				.filter(entry -> {
					return !entry.getValue().entrySet().stream()
							.filter(entry2 -> entry2.getValue().isEmpty()) // いずれかの倍率にタイプが存在しない場合を省く。
							.anyMatch(e -> true);
				})
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		final int count = twoTypeKeyList.size();
		twoTypeKeyList.forEach(e -> {
			// put
			putMsg(e, MessageFormat.format(
					msgFormat,
					e.getType1().getJpn(),
					e.getType2().getJpn(),
					count));
		});
	}

	/**
	 * 1種族しか存在しないタイプの組み合わせのポケモンである旨を示すメッセージを作成する。
	 *
	 * @param goPokedexRepository
	 * @param evolutionProvider
	 * @param msgFormat
	 */
	private void onlyTwoType(
			GoPokedexRepository goPokedexRepository,
			EvolutionProvider evolutionProvider,
			String msgFormat) {

		// GOポケモン図鑑を全て取得する
		List<GoPokedex> goPokedexList = goPokedexRepository.findAll();

		// key: TwoTypeKey, value: Set<GoPokedex>のMapを作成する。
		Map<TwoTypeKey, List<GoPokedex>> typeGpMap = goPokedexList.stream()
				// key: TwoTypeKey, value: List<GoPokedex>に持ち変える。
				.collect(Collectors.groupingBy(gp -> new TwoTypeKey(TypeEnum.getType(gp.getType1()), TypeEnum.getType(gp.getType2()))))
				.entrySet().stream()
				.filter(entry -> {
					// key: pokedexNo, value pokedexIdのリストを保持するMapを一旦作成する。
					Map<Integer, List<String>> pokeNoMap = entry.getValue().stream()
							.map(GoPokedex::getPokedexId)
							.collect(Collectors.groupingBy(pid -> evolutionProvider.basePokedexNo(pid))); // 同系統のポケモンごとに括る。
					return pokeNoMap.size() == 1; // 同系統のポケモンが1件のポケモンのみに絞り込む。

				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Mapをもとにメッセージをputする。
		typeGpMap.entrySet().stream().forEach(entry -> {
			final TwoTypeKey ttKey = entry.getKey();
			final List<String> pokeNameList = entry.getValue().stream()
					.sorted(PokemonEditUtils.getPokedexComparator(1)) // 昇順で並び替え
					.map(gp -> PokemonEditUtils.appendRemarks(gp)) // 備考を連結
					.collect(Collectors.toList());

			// put
			putMsg(ttKey, MessageFormat.format(
					msgFormat,
					ttKey.getType1().getJpn(),
					ttKey.getType2().getJpn(),
					StringUtils.join(pokeNameList, ", ")));
		});
	}

	/**
	 * 最も定番の組み合わせのタイプである旨のメッセージを作成する。
	 *
	 * @param goPokedexRepository
	 * @param evolutionProvider
	 * @param msgFormat
	 */
	private void mostPopularTwoType(
			GoPokedexRepository goPokedexRepository,
			EvolutionProvider evolutionProvider,
			String msgFormat) {

		// GOポケモン図鑑を全て取得する
		List<GoPokedex> goPokedexList = goPokedexRepository.findAll();

		// key: TwoTypeKey, value: Map<pokedexNo, List<pokedexId>のMapを作成する。
		Map<TwoTypeKey, Map<Integer, List<String>>> typeTreeMap = goPokedexList.stream()
				.filter(gp -> !StringUtils.isEmpty(gp.getType1()) && !StringUtils.isEmpty(gp.getType2())) // 1タイプのポケモンを除去する。
				// key: TwoTypeKey, value: List<GoPokedex>に持ち変える。
				.collect(Collectors.groupingBy(gp -> new TwoTypeKey(TypeEnum.getType(gp.getType1()), TypeEnum.getType(gp.getType2()))))
				.entrySet().stream()
				.collect(Collectors.toMap(
						entry -> entry.getKey(),
						entry -> {
							return entry.getValue().stream()
									.map(GoPokedex::getPokedexId)
									.collect(Collectors.groupingBy(
											pid -> evolutionProvider.basePokedexNo(pid))); // 同系統のポケモンごとに括る。(Map<pokedexNo, List<pokedexId>>)
						}));

		final int max = typeTreeMap.entrySet().stream()
				.max((o1, o2) -> o1.getValue().size() - o2.getValue().size()) // 同系統のポケモンで括って系統数が最大のタイプを取得する。
				.get().getValue().size(); // 系統数を取得する。

		// 最も多い組み合わせのタイプにおける、key: TwoTypeKey, value: GoPokedexのリストを作成する。
		Map<TwoTypeKey, List<GoPokedex>> maxTypeGpMap = typeTreeMap.entrySet().stream()
				.filter(entry -> entry.getValue().size() == max) // 系統数が最大のタイプに絞り込む。
				.collect(Collectors.toMap(
						entry -> entry.getKey(),
						entry -> {
							return entry.getValue().entrySet().stream()
									.flatMap(entry2 -> entry2.getValue().stream()) // タイプごとのポケモンを直列化。
									.map(pid -> goPokedexRepository.findById(pid).get()) // goPokedexを取得。
									.collect(Collectors.toList());
						}));

		// Mapをもとにメッセージをputする。
		maxTypeGpMap.entrySet().stream().forEach(entry -> {
			final TwoTypeKey ttKey = entry.getKey();
			final List<String> pokeNameList = entry.getValue().stream()
					.sorted(PokemonEditUtils.getPokedexComparator(1)) // 昇順で並び替え
					.map(gp -> PokemonEditUtils.appendRemarks(gp)) // 備考を連結
					.collect(Collectors.toList());

			// put
			putMsg(ttKey, MessageFormat.format(
					msgFormat,
					ttKey.getType1().getJpn(),
					ttKey.getType2().getJpn(),
					pokeNameList.size(),
					StringUtils.join(pokeNameList, ", ")));
		});
	}

	/**
	 * 存在しない組み合わせの2タイプであることを伝えるメッセージを作成する。
	 *
	 * @param goPokedexRepository
	 * @param msgFormat
	 */
	private void notExistsTwoType(GoPokedexRepository goPokedexRepository, String msgFormat) {

		// GOポケモン図鑑を全て取得する
		List<GoPokedex> goPokedexList = goPokedexRepository.findAll();

		// 存在するタイプの組み合わせを洗い出す。
		List<TwoTypeKey> twoTypeList = goPokedexList.stream()
				.map(gp -> new TwoTypeKey(TypeEnum.getType(gp.getType1()), TypeEnum.getType(gp.getType2())))
				.distinct()
				.collect(Collectors.toList());

		// 存在しないタイプの組み合わせを洗い出す。
		List<TwoTypeKey> noMatchTwoTypeList = TypeUtils.getTwoTypeStreamContainsOneType()
				.filter(ttk -> !twoTypeList.contains(ttk)) // 存在するタイプを省く。
				.collect(Collectors.toList());

		noMatchTwoTypeList.forEach(ttKey -> {
			// put
			putMsg(ttKey, MessageFormat.format(
					msgFormat,
					ttKey.getType1().getJpn(),
					ttKey.getType2().getJpn()));
		});
	}

	/**
	 * TypeEffectiveEnumのいずれかの列挙子から、最もタイプ数が多い物に対して、メッセージを追加する。
	 *
	 * @param typeChartInfo
	 * @param msgFormat
	 * @param typeEff
	 */
	private void maxCountAttackerDamageMult(
			TypeChartInfo typeChartInfo,
			String msgFormat,
			TypeEffectiveEnum typeEff) {

		if (typeEff == TypeEffectiveEnum.MAX || typeEff == TypeEffectiveEnum.MIN) return; // あり得ないパターン

		final Map<TypeEnum, List<TypeEnum>> typeMap = new HashMap<>();
		for (TypeEnum te: TypeEnum.values()) {
			Map<TypeEffectiveEnum, List<TypeEnum>> typeEffMap = typeChartInfo.getAttackerTypes(te);
			typeMap.put(te, typeEffMap.get(typeEff));

		}

		// タイプ数の最大数を求める。
		final int max = typeMap.entrySet().stream()
				.map(entry -> entry.getValue())
				.max((o1, o2) -> o1.size() - o2.size())
				.get().size();

		Map<TypeEnum, List<String>> maxCntTypeMap = typeMap.entrySet().stream()
				.filter(entry -> entry.getValue().size() == max) // タイプの最大数で絞り込む。
				.collect(Collectors.toMap(
						entry -> entry.getKey(),
						entry -> {
							return entry.getValue().stream()
									.map(te -> te.getJpn()) // タイプを日本語に変換。
									.collect(Collectors.toList());
						}));

		// TwoTypeKeyをキーとして、こうげき時の倍率が第3引数どおりのタイプ
		Map<TwoTypeKey, Map<TypeEnum, List<String>>> ttKeySetMap = maxCntTypeMap.entrySet().stream()
				.flatMap(entry -> {
					// 1タイプ目はタイプ数が倍率ごとに最大のタイプ。2タイプ目をくっつけてTwoTypeKeyにする。
					TypeEnum te1 = entry.getKey();
					return Arrays.stream(TypeEnum.values())
							.map(te2 -> new TwoTypeKey(te1, te2));
				})
				.collect(Collectors.toSet())
				.stream()
				.collect(Collectors.toMap(
						ttk -> ttk,
						ttk -> {
							return maxCntTypeMap.entrySet().stream()
									// TwoTypeKeyにmaxCntTypeMapのキーのTypeEnumが含まれている場合
									.filter(entry -> entry.getKey() == ttk.getType1() || entry.getKey() == ttk.getType2())
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // 重複を削除
						}));

		// メッセージを生成
		ttKeySetMap.forEach((ttk, set) -> {
			set.forEach((type, list) -> {
				// put
				putMsg(ttk, MessageFormat.format(
						msgFormat,
						type.getJpn(),
						String.valueOf(typeEff.getDamageMultiplier()),
						prependClassifier(list.size()),
						StringUtils.join(list, ", ")));
			});
		});

	}

	/**
	 * TypeEffectiveEnumのいずれかの列挙子から、最もタイプ数が多い物に対して、メッセージを追加する。
	 *
	 * @param typeChartInfo
	 * @param msgFormat
	 * @param typeEff
	 */
	private void maxCountDefenderDamageMult(
			TypeChartInfo typeChartInfo,
			String msgFormat,
			TypeEffectiveEnum typeEff) {

		// 1タイプの場合も確認対象。
		Map<TwoTypeKey, List<TypeEnum>> ttKeyMap = TypeUtils.getTwoTypeStreamContainsOneType()
				.collect(Collectors.toMap(
						ttk -> ttk,
						ttk -> typeChartInfo.getDefenderTypes(ttk.getType1(), ttk.getType2()).get(typeEff)));

		// タイプ数の最大数を求める。
		final int max = ttKeyMap.entrySet().stream()
				.map(entry -> entry.getValue())
				.max((o1, o2) -> o1.size() - o2.size())
				.get().size();

		ttKeyMap.entrySet().stream()
		.filter(entry -> entry.getValue().size() == max) // タイプの最大数で絞り込む。
		.collect(Collectors.toMap(
				entry -> entry.getKey(),
				entry -> {
					return entry.getValue().stream()
							.map(te -> te.getJpn()) // タイプを日本語に変換。
							.collect(Collectors.toList());
				}))
		.entrySet().stream()
		.forEach(entry -> {
			// put
			putMsg(entry.getKey(), MessageFormat.format(
					msgFormat,
					entry.getKey().toJpnString(),
					String.valueOf(typeEff.getDamageMultiplier()),
					prependClassifier(entry.getValue().size()),
					StringUtils.join(entry.getValue(), ", ")));
		});
	}

	/**
	 * 弱点タイプが最も少ないタイプの組み合わせを洗い出し、メッセージを作成する。。
	 *
	 * @param typeChartInfo
	 * @param msgFormat
	 */
	private void leastWeaknessType(TypeChartInfo typeChartInfo, String msgFormat) {

		// タイプごとに弱点タイプを保持したマップを作成する。
		Map<TwoTypeKey, List<TypeEnum>> weaknessMap = TypeUtils.getTwoTypeStreamContainsOneType()
				.collect(Collectors.toMap( // collectし、Valueをくっつけてマップにする。
						ttk -> ttk,
						ttk -> {
							return typeChartInfo.getDefenderTypes(ttk.getType1(), ttk.getType2()) // Map<各倍率: List<タイプ>>を取得
									.entrySet().stream()
									// 弱点タイプにだけ絞り込む。
									.filter(entry -> entry.getKey() == TypeEffectiveEnum.HIGH || entry.getKey() == TypeEffectiveEnum.MAX)
									.flatMap(entry -> entry.getValue().stream())
									.collect(Collectors.toList());
						}));

		// 弱点が最も少ない組み合わせの弱点の数
		int min = weaknessMap.entrySet().stream()
				.map(entry -> entry.getValue().size())
				.min((o1, o2) -> o1 - o2)
				.get();

		// 弱点が最も少ないタイプが何個あるか。
		int leastWeeknessCnt = (int) weaknessMap.entrySet().stream()
				.filter(entry -> entry.getValue().size() == min)
				.count();

		// メッセージをputする
		weaknessMap.entrySet().stream()
		.filter(entry -> entry.getValue().size() == min) // 弱点が最も少ないタイプに絞り込む
		.forEach(entry -> {
			TwoTypeKey ttk = entry.getKey();
			// 弱点のタイプのリストを日本語名で取得する。
			List<String> weaknessList = entry.getValue().stream()
					.map(TypeEnum::getJpn)
					.collect(Collectors.toList());

			// put
			putMsg(ttk, MessageFormat.format(
					msgFormat,
					ttk.toJpnString(),
					prependClassifier(min),
					prependClassifier(leastWeeknessCnt),
					StringUtils.join(weaknessList, ", ")));
		});
	}



	/**
	 * 助数詞を連結します。
	 *
	 * @param wordCount
	 * @return
	 */
	private String prependClassifier(int wordCount) {
		return String.valueOf(wordCount) + (wordCount < 10 ? "つ": "こ");
	}

	/**
	 * 第１引数に該当するkeyのvalueにもつListにメッセージを追加する。
	 *
	 *
	 * @param key
	 * @param msg
	 */
	private void putMsg(TwoTypeKey key, String msg) {
		// msgMapのvalueがnullの場合は、リストを生成してadd、ある場合はそのインスタンスにadd。
		computeIfAbsent(key, li -> new LinkedHashSet<>()).add(msg);
	}
}
