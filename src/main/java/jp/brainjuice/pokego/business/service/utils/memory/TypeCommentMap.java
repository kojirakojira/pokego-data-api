package jp.brainjuice.pokego.business.service.utils.memory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.constant.Type.TypeEffectiveEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;

@Component
public class TypeCommentMap extends HashMap<TwoTypeKey, LinkedHashSet<String>> {

	@Autowired
	public TypeCommentMap(
			TypeChartInfo typeChartInfo,
			GoPokedexRepository goPokedexRepository,
			EvolutionInfo evolutionInfo) throws PokemonDataInitException {

		// メッセージ作成
		createMessageMap(typeChartInfo, goPokedexRepository, evolutionInfo);
	}

	public LinkedHashSet<String> get(TypeEnum te1, TypeEnum te2) {
		return get(new TwoTypeKey(te1, te2));
	}

	/**
	 * メッセージを作成してmsgMapにputする。
	 *
	 * @param typeChartInfo
	 * @param goPokedexRepository
	 * @param evolutionInfo
	 * @throws PokemonDataInitException
	 */
	public void createMessageMap(
			TypeChartInfo typeChartInfo,
			GoPokedexRepository goPokedexRepository,
			EvolutionInfo evolutionInfo) throws PokemonDataInitException {

		// 1種族しかない組み合わせ
		onlyTwoType(goPokedexRepository, evolutionInfo, "{0}, {1}は1種族しか存在しない組み合わせです。（対象ポケモン：{2}）");

		// 最も定番の組み合わせ
		mostPopularTwoType(goPokedexRepository, evolutionInfo, "{0}, {1}は最も定番の組み合わせで、{2}体のポケモンが該当します。（対象ポケモン：{3}）");

		// 存在しない組み合わせ
		notExistsTwoType(goPokedexRepository, "{0}, {1}は、現在のポケモンにおいて存在しない組み合わせです。");

		// こうげき、ぼうぎょ時のスコア
		maxScore(typeChartInfo, "こうげき時の評価が最高評価です！", () -> getAttackerScoreMap(typeChartInfo));
		minScore(typeChartInfo, "こうげき時の評価が最低評価です…。", () -> getAttackerScoreMap(typeChartInfo));
		maxScore(typeChartInfo, "ぼうぎょ時の評価が最高評価です！", () -> getDefenderScoreMap(typeChartInfo));
		minScore(typeChartInfo, "ぼうぎょ時の評価が最低評価です…。", () -> getDefenderScoreMap(typeChartInfo));

		// めっぽう弱い
		weak(typeChartInfo, "{0}のこうげきに対してめっぽう弱いです…。(×2.56倍)", TypeEffectiveEnum.MAX);
		// とてつもない耐性
		weak(typeChartInfo, "{0}のこうげきに対してとてつもなく耐性があります。(×0.244140625倍)", TypeEffectiveEnum.MIN);
		// 強い耐性
		weak(typeChartInfo, "{0}のこうげきに対して強い耐性があります。(×0.390625倍)", TypeEffectiveEnum.VERY_LOW);
		// 唯一の×2.56
		onlyOneType(typeChartInfo, "{0}のこうげきに対して唯一×2.56倍のダメージ倍率が出ます。", TypeEffectiveEnum.MAX);
		// 唯一の×0.244140625
		onlyOneType(typeChartInfo, "{0}のこうげきに対して唯一×0.244140625倍のダメージ倍率が出ます。", TypeEffectiveEnum.MIN);
		// 唯一の×0.390625
		onlyOneType(typeChartInfo, "{0}のこうげきに対して唯一×0.390625倍のダメージ倍率が出ます。", TypeEffectiveEnum.VERY_LOW);
		// すべてのダメージ倍率
		allDamageMultiplier(typeChartInfo, "{0}, {1}は、全ての倍率でこうげきを受けうるタイプの組み合わせです。これは、全組み合わせ中{2}タイプのみです。");

		// ダメージ倍率数の最大（こうげき）
		{
			String strengthMsgFormat = "{0}は、こうげき時に×{1}倍のダメージ倍率が出るタイプの数が、{2}あります。これは全タイプ中最も多く、こうげき面で優秀です。（対象タイプ：{3}）";
			String weakMsgFormat = "{0}は、こうげき時に×{1}倍のダメージ倍率が出るタイプの数が、{2}あります。これは全タイプ中最も多く、こうげき面に難ありです…。（対象タイプ：{3}）";
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
			String weakMsgFormat = "{0}は、ぼうぎょ時に×{1}倍のダメージ倍率が出るタイプの数が、{2}あります。これは全タイプ中最も多く、ぼうぎょ面に難ありです…。（対象タイプ：{3}）";
			String strengthMsgFormat = "{0}は、ぼうぎょ時に×{1}倍のダメージ倍率が出るタイプの数が、{2}あります。これは全タイプ中最も多く、ぼうぎょ面で優秀です。（対象タイプ：{3}）";
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

	private Map<TwoTypeKey, Double> getAttackerScoreMap(TypeChartInfo typeChartInfo) {

		// 1タイプの考慮
		TypeEnum[] values = addedNullTypeEnum();

		Map<TwoTypeKey, Double> scoreMap = new HashMap<>();
		for (TypeEnum te1: values) {
			for (TypeEnum te2: values) {
				if (te1 == te2 || te1 == null) continue;

				TwoTypeKey ttKey = new TwoTypeKey(te1, te2);

				double score = typeChartInfo.attackerScore(te1);
				if (te2 != null) {
					score += typeChartInfo.attackerScore(te2);
					score /= 2;
				}

				scoreMap.put(ttKey, Double.valueOf(score));
			}
		}

		return scoreMap;
	}

	private Map<TwoTypeKey, Double> getDefenderScoreMap(TypeChartInfo typeChartInfo) {

		// 1タイプの考慮
		TypeEnum[] values = addedNullTypeEnum();

		Map<TwoTypeKey, Double> scoreMap = new HashMap<>();
		for (TypeEnum te1: values) {
			for (TypeEnum te2: values) {
				if (te1 == te2 || te1 == null) continue;

				TwoTypeKey ttKey = new TwoTypeKey(te1, te2);

				double score = typeChartInfo.defenderScore(te1, te2);

				scoreMap.put(ttKey, Double.valueOf(score));
			}
		}

		return scoreMap;
	}

	/**
	 * 特定のタイプから、その倍率のダメージ倍率になるタイプを取得し、その旨を示すメッセージを作成します。
	 *
	 * @param typeChartInfo
	 * @param msgFormat
	 * @param typeEff
	 */
	private void weak(TypeChartInfo typeChartInfo, String msgFormat, TypeEffectiveEnum typeEff) {

		for (TypeEnum te1: TypeEnum.values()) {
			for (TypeEnum te2: TypeEnum.values()) {
				if (te1 == te2) continue;
				Map<TypeEffectiveEnum, List<TypeEnum>> typeMap = typeChartInfo.getDefenderTypes(te1, te2);

				List<String> typeList = typeMap.get(typeEff).stream().map(TypeEnum::getJpn).collect(Collectors.toList());

				if (typeList.isEmpty()) {
					continue;
				}

				String msg = MessageFormat.format(msgFormat, StringUtils.join(typeList, ", "));
				putMsg(te1, te2, msg);

			}
		}
	}

	/**
	 * 特定のタイプから、唯一その倍率のダメージ倍率になるタイプを取得し、その旨を示すメッセージを作成します。
	 *
	 * @param typeChartInfo
	 * @param msgFormat
	 * @param typeEff
	 */
	private void onlyOneType(TypeChartInfo typeChartInfo, String msgFormat, TypeEffectiveEnum typeEff) {

		final Map<TwoTypeKey, List<TypeEnum>> typeMap = new HashMap<>();

		// こうげき倍率が×2.56になるタイプのリストを作成する。
		for (TypeEnum te1: TypeEnum.values()) {
			for (TypeEnum te2: TypeEnum.values()) {
				if (te1 == te2) continue;
				final Map<TypeEffectiveEnum, List<TypeEnum>> typeEffMap = typeChartInfo.getDefenderTypes(te1, te2);

				final List<TypeEnum> tmpTypeList = typeEffMap.get(typeEff);

				if (tmpTypeList.isEmpty()) {
					continue;
				}

				typeMap.put(new TwoTypeKey(te1, te2), tmpTypeList);

			}
		}

		Map<TwoTypeKey, TypeEnum> twoTypeMap = new HashMap<>();
		for (TypeEnum type: TypeEnum.values()) {
			boolean isDuplication = false;
			TwoTypeKey tmpTwoTypeKey = null;
			for (Map.Entry<TwoTypeKey, List<TypeEnum>> entry: typeMap.entrySet()) {

				if (entry.getValue().contains(type)) {
					if (tmpTwoTypeKey != null) {
						isDuplication = true;
						break;
					}
					tmpTwoTypeKey = entry.getKey();
				}
			}
			if (tmpTwoTypeKey != null && !isDuplication) {
				twoTypeMap.put(tmpTwoTypeKey, type);
			}
		}

		twoTypeMap.forEach((k, v) -> {
			putMsg(k, MessageFormat.format(msgFormat, v.getJpn()));
		});
	}

	/**
	 * すべてのダメージ倍率を網羅している旨を伝えるメッセージを作成する。
	 *
	 * @param typeChartInfo
	 * @param msgFormat
	 */
	private void allDamageMultiplier(TypeChartInfo typeChartInfo, String msgFormat) {

		Set<TwoTypeKey> twoTypeKeySet = new HashSet<>();

		for (TypeEnum te1: TypeEnum.values()) {
			for (TypeEnum te2: TypeEnum.values()) {
				final Map<TypeEffectiveEnum, List<TypeEnum>> effMap = typeChartInfo.getDefenderTypes(te1, te2);

				if (effMap.entrySet().stream().filter(
						entry -> entry.getValue().isEmpty()).anyMatch(e -> true)) {
					// 空のリストが含まれていたらスキップする。
					continue;
				}

				twoTypeKeySet.add(new TwoTypeKey(te1, te2));
			}
		}

		final int count = twoTypeKeySet.size();
		twoTypeKeySet.forEach(e -> {
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
	 * @param evolutionInfo
	 * @param msgFormat
	 */
	private void onlyTwoType(
			GoPokedexRepository goPokedexRepository,
			EvolutionInfo evolutionInfo,
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
							.collect(Collectors.groupingBy(pid -> evolutionInfo.basePokedexNo(pid))); // 同系統のポケモンごとに括る。
					return pokeNoMap.size() == 1; // 同系統のポケモンが1件のポケモンのみに絞り込む。

				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Mapをもとにメッセージをputする。
		typeGpMap.entrySet().stream().forEach(entry -> {
			final TwoTypeKey ttKey = entry.getKey();
			final List<String> pokeNameList = entry.getValue().stream()
					.sorted(PokemonEditUtils.getPokedexComparator()) // 昇順で並び替え
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
	 * @param evolutionInfo
	 * @param msgFormat
	 */
	private void mostPopularTwoType(
			GoPokedexRepository goPokedexRepository,
			EvolutionInfo evolutionInfo,
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
											pid -> evolutionInfo.basePokedexNo(pid))); // 同系統のポケモンごとに括る。(Map<pokedexNo, List<pokedexId>>)
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
					.sorted(PokemonEditUtils.getPokedexComparator()) // 昇順で並び替え
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

	private void notExistsTwoType(GoPokedexRepository goPokedexRepository, String msgFormat) {

		// GOポケモン図鑑を全て取得する
		List<GoPokedex> goPokedexList = goPokedexRepository.findAll();

		Set<TwoTypeKey> twoTypeSet = goPokedexList.stream()
				.map(gp -> new TwoTypeKey(TypeEnum.getType(gp.getType1()), TypeEnum.getType(gp.getType2())))
				.collect(Collectors.toSet());

		// 一応、1タイプの場合もポケモンが存在しない場合がある。（実際はないけど。）
		TypeEnum[] values = addedNullTypeEnum();

		Set<TwoTypeKey> noMatchTwoTypeSet = new HashSet<>();
		for (TypeEnum te1: values) {
			for (TypeEnum te2: values) {
				if (te1 == te2) continue;

				TwoTypeKey twoTypeKey = new TwoTypeKey(te1, te2);
				if (twoTypeSet.contains(twoTypeKey)) continue; // 既に追加している場合はスキップ。

				noMatchTwoTypeSet.add(twoTypeKey);
			}
		}

		noMatchTwoTypeSet.forEach(ttKey -> {
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
		final TypeEnum[] values = addedNullTypeEnum();

		final Map<TwoTypeKey, List<TypeEnum>> ttKeyMap = new HashMap<>();
		for (TypeEnum te1: values) {
			for (TypeEnum te2: values) {
				if (te1 == te2 || te1 == null) continue;

				TwoTypeKey ttKey = new TwoTypeKey(te1, te2);
				if (ttKeyMap.containsKey(ttKey)) continue;

				Map<TypeEffectiveEnum, List<TypeEnum>> typeEffMap = typeChartInfo.getDefenderTypes(te1, te2);
				ttKeyMap.put(ttKey, typeEffMap.get(typeEff));
			}
		}

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
					getTwoTypeJpn(entry.getKey()),
					String.valueOf(typeEff.getDamageMultiplier()),
					prependClassifier(entry.getValue().size()),
					StringUtils.join(entry.getValue(), ", ")));
		});
	}

	/**
	 * TypeEnum[]の一番後ろにnullを追加した配列を返却する。
	 *
	 * @return
	 */
	private TypeEnum[] addedNullTypeEnum() {

		final TypeEnum[] arr = TypeEnum.values();
		final TypeEnum[] values = new TypeEnum[arr.length + 1];
		System.arraycopy(arr, 0, values, 0, arr.length);

		return values;
	}

	/**
	 * 2タイプの日本語名を連結して返却します。
	 *
	 * @param twoTypeKey
	 * @return
	 */
	private String getTwoTypeJpn(TwoTypeKey twoTypeKey) {

		StringBuilder sb = new StringBuilder();

		if (twoTypeKey.getType1() != null) {
			sb.append(twoTypeKey.getType1().getJpn());
			sb.append(", ");
		}

		if (twoTypeKey.getType2() != null) {
			sb.append(twoTypeKey.getType2().getJpn());
		}

		return sb.toString();
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

	private void putMsg(TypeEnum type1, TypeEnum type2, String msg) {
		TwoTypeKey key = new TwoTypeKey(type1, type2);
		putMsg(key, msg);
	}

	private void putMsg(TwoTypeKey key, String msg) {
		// msgMapのvalueがnullの場合は、リストを生成してadd、ある場合はそのインスタンスにadd。
		computeIfAbsent(key, li -> new LinkedHashSet<>()).add(msg);
	}
}
