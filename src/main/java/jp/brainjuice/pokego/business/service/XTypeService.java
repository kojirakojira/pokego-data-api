package jp.brainjuice.pokego.business.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.Type.TypeEffectiveEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.utils.dto.XTypeElement;
import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;
import jp.brainjuice.pokego.business.service.utils.memory.TypeChartInfo;
import jp.brainjuice.pokego.business.service.utils.memory.TypeChartInfo.EmphasisEnum;
import jp.brainjuice.pokego.business.service.utils.memory.TypeCommentMap;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.XTypeResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class XTypeService {

	private TypeChartInfo typeChartInfo;

	private TypeCommentMap typeCommentMap;

	private static final String X = "x";

	private static final int OWN_TYPE1 = 0;
	private static final int OWN_TYPE2 = 1;
	private static final int OPP_TYPE1 = 2;
	private static final int OPP_TYPE2 = 3;

	@Autowired
	public XTypeService(TypeChartInfo typeChartInfo,
			TypeCommentMap typeCommentMap) {
		this.typeChartInfo = typeChartInfo;
		this.typeCommentMap = typeCommentMap;
	}

	public boolean check(
			String ownType1,
			String ownType2,
			String oppType1,
			String oppType2,
			String emphasis,
			XTypeResponse res) {

		// 一旦成功とする。
		res.setSuccess(true);

		// タイプのエラー判定に使用する関数
		Function<String, Boolean> isTypeNotDefined =
				(type) -> !StringUtils.isEmpty(type) && !type.equals(X) && !TypeEnum.isDefined(type);
		// エラー時に呼び出す関数
		Consumer<String> setError = (msg) -> {
			res.setSuccess(false);
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage(res.getMessage() + msg);
		};

		if (isTypeNotDefined.apply(ownType1)) {
			setError.accept("「じぶんのポケモン」のタイプ１に誤りがあります。\n");
		}

		if (isTypeNotDefined.apply(ownType2)) {
			setError.accept("「じぶんのポケモン」のタイプ２に誤りがあります。\n");
		}

		if (isTypeNotDefined.apply(oppType1)) {
			setError.accept("「あいてのポケモン」のタイプ１に誤りがあります。\n");
		}

		if (isTypeNotDefined.apply(oppType2)) {
			setError.accept("「あいてのポケモン」のタイプ２に誤りがあります。\n");
		}

		if (StringUtils.isEmpty(emphasis) || !EmphasisEnum.isDefined(emphasis)) {
			setError.accept("重視ポイントは正しく入力してください。");
		}

		return res.isSuccess();
	}

	/**
	 * 実行する。
	 *
	 * @param ownType1
	 * @param ownType2
	 * @param oppType1
	 * @param oppType2
	 * @param emphasis
	 * @param res
	 */
	public void exec(
			String ownType1,
			String ownType2,
			String oppType1,
			String oppType2,
			String emphasis,
			XTypeResponse res) {

		// タイプ1またはタイプ2のみを定義している場合を考慮して、Optional型で扱う。
		Optional<String> ownType1Op = Optional.ofNullable(StringUtils.isEmpty(ownType1) ? null : ownType1);
		Optional<String> ownType2Op = Optional.ofNullable(StringUtils.isEmpty(ownType2) ? null : ownType2);
		Optional<String> oppType1Op = Optional.ofNullable(StringUtils.isEmpty(oppType1) ? null : oppType1);
		Optional<String> oppType2Op = Optional.ofNullable(StringUtils.isEmpty(oppType2) ? null : oppType2);

		if (!EmphasisEnum.isDefined(emphasis)) {
			throw new IllegalArgumentException("EmphasisEnumに定義していない値が設定されました。" + emphasis);
		}

		// 何を重視してスコアを求めるか
		EmphasisEnum emphasisEnum = EmphasisEnum.valueOf(emphasis);

		res.setOwn1(ownType1);
		res.setOwn2(ownType2);
		res.setOpp1(oppType1);
		res.setOpp2(oppType2);
		res.setEmphasis(emphasisEnum.getJpn());

		List<XTypeElement> typeRankMap = getTypeRankList(
				ownType1Op, ownType2Op, oppType1Op, oppType2Op, emphasisEnum);
		res.setTypeRankList(typeRankMap);

		LinkedHashSet<String> comments = getCommentsMap(ownType1Op, ownType2Op, oppType1Op, oppType2Op);
		res.setTypeComments(comments);

	}

	/**
	 * ランク順のタイプのリストを取得する。
	 *
	 * @param ownType1Op
	 * @param ownType2Op
	 * @param oppType1Op
	 * @param oppType2Op
	 * @param emphasisEnum
	 * @return
	 */
	private List<XTypeElement> getTypeRankList(
			Optional<String> ownType1Op,
			Optional<String> ownType2Op,
			Optional<String> oppType1Op,
			Optional<String> oppType2Op,
			EmphasisEnum emphasisEnum) {

		// Xがどこに仮定されているか
		int xTypePosition = getXTypePosition(ownType1Op, ownType2Op, oppType1Op, oppType2Op);

		// Xを定義したのが、こうげき側であるかどうか。
		boolean isXAtk = xTypePosition == OWN_TYPE1 || xTypePosition == OWN_TYPE2;

		List<BattlePattern> battlePatternList = getBattlePatternList(
				ownType1Op, ownType2Op, oppType1Op, oppType2Op, isXAtk);

		// スコア順のタイプリストを取得する。
		final LinkedHashMap<TwoTypeKey, Double> sortedScoreTypeMap = getSortedTypeMap(battlePatternList, emphasisEnum, isXAtk);

		// TwoTypeKeyごとに、じぶんがあいてに攻撃した際のスコアを取得する。
		final Map<TwoTypeKey, List<String>> atkMsgMap = battlePatternList.stream().collect(Collectors.toMap(
				(bp -> isXAtk ? bp.getOwnType() : bp.getOppType()),
				(bp -> getAtkMsgList(bp))));

		// TwoTypeKeyごとに、じぶんがあいてから攻撃を受けた際のスコアを取得する。
		final Map<TwoTypeKey, List<String>> defMsgMap = battlePatternList.stream().collect(Collectors.toMap(
				(bp -> isXAtk ? bp.getOwnType() : bp.getOppType()),
				(bp -> getDefMsgList(bp))));

		// スコア順に順位付けをする。
		// 順位付けした結果はList<XTypeElement>で保持する。
		final List<XTypeElement> xTypeList = new ArrayList<>();
		{
			int rank = 0;
			double tmpScore = 0;
			for (Map.Entry<TwoTypeKey, Double> entry: sortedScoreTypeMap.entrySet()) {

				double score = entry.getValue().doubleValue();
				if (Math.abs(score - tmpScore) > 0.0001) {
					// scoreが異なる場合、ランクを1インクリメントさせる。
					rank+=1;
				}

				// XTypeリストに追加。
				xTypeList.add(new XTypeElement(
						entry.getKey(),
						rank,
						atkMsgMap.get(entry.getKey()),
						defMsgMap.get(entry.getKey())));

				// スコアを退避
				tmpScore = score;
			}
		}

		return xTypeList;
	}

	private int getXTypePosition(
			Optional<String> ownType1Op,
			Optional<String> ownType2Op,
			Optional<String> oppType1Op,
			Optional<String> oppType2Op) {

		int xTypePosition;

		Function<Optional<String>, Boolean> isX = ops -> ops.isPresent() && ops.get().equals(X);
		if (isX.apply(ownType1Op)) {
			xTypePosition = OWN_TYPE1;
		} else if (isX.apply(ownType2Op)) {
			xTypePosition = OWN_TYPE2;
		} else if (isX.apply(oppType1Op)) {
			xTypePosition = OPP_TYPE1;
		} else if (isX.apply(oppType2Op)) {
			xTypePosition = OPP_TYPE2;
		} else {
			throw new IllegalArgumentException("引数のいずれかにxの文字を渡す必要があります。");
		}

		return xTypePosition;
	}

	/**
	 * じぶん、あいてのタイプの全パターンを洗い出し、リストにして返却する。<br>
	 * 引数のタイプには、いずれかにxの文字列がある。そのタイプを全部のタイプに当て込むことによって、全パターンを網羅する。
	 *
	 * @param ownType1Op
	 * @param ownType2Op
	 * @param oppType1Op
	 * @param oppType2Op
	 * @return
	 */
	private List<BattlePattern> getBattlePatternList(
			Optional<String> ownType1Op,
			Optional<String> ownType2Op,
			Optional<String> oppType1Op,
			Optional<String> oppType2Op,
			boolean isXAtk) {

		// Xがどこに仮定されているか
		final int xTypePosition = getXTypePosition(ownType1Op, ownType2Op, oppType1Op, oppType2Op);

		// Xで定義していない、かつ値がある場合はTypeEnumを返却し、それ以外はnullを返却する関数。
		BiFunction<Optional<String>, Integer, TypeEnum> func = (ops, i) -> {
			return xTypePosition != i && ops.isPresent() ? TypeEnum.valueOf(ops.get()) : null;
		};

		final TypeEnum ownType1 = func.apply(ownType1Op, OWN_TYPE1);
		final TypeEnum ownType2 = func.apply(ownType2Op, OWN_TYPE2);
		final TypeEnum oppType1 = func.apply(oppType1Op, OPP_TYPE1);
		final TypeEnum oppType2 = func.apply(oppType2Op, OPP_TYPE2);

		final List<BattlePattern> battlePatternList = Arrays.stream(TypeEnum.values())
				.map(te -> {
					TwoTypeKey ownTtKey = new TwoTypeKey(
							(xTypePosition == OWN_TYPE1 ? te : ownType1),
							(xTypePosition == OWN_TYPE2 ? te : ownType2));
					TwoTypeKey oppTtKey = new TwoTypeKey(
							(xTypePosition == OPP_TYPE1 ? te : oppType1),
							(xTypePosition == OPP_TYPE2 ? te : oppType2));
					BattlePattern bp = new BattlePattern(ownTtKey, oppTtKey);
					return bp;
				})
				.filter(bp -> {
					final TwoTypeKey ttk = isXAtk ? bp.getOwnType() : bp.getOppType();
					return ttk.getType1() != ttk.getType2(); // Xと仮定したタイプと、そのペアのタイプが一致する場合は、排除する。
				})
				.collect(Collectors.toList());

		return battlePatternList;
	}

	/**
	 * スコア順に降順に並び替えられたスコアのマップを返却する。
	 *
	 * @param battlePatternList
	 * @param emphasisEnum
	 * @param isXAtk
	 * @return
	 */
	private LinkedHashMap<TwoTypeKey, Double> getSortedTypeMap(
			List<BattlePattern> battlePatternList,
			EmphasisEnum emphasisEnum,
			boolean isXAtk) {

		return battlePatternList.stream()
					.collect(Collectors.toMap(
							// Xを定義していない側のタイプをキーとする。（Xが自分だったら相手、Xが相手だったら自分。）
							bp -> (isXAtk ? bp.getOwnType(): bp.getOppType()),
							// Valueはスコア
							bp -> {
								return Double.valueOf(typeChartInfo.score(
										bp.getOwnType(),
										bp.getOppType(),
										emphasisEnum)); // スコアを求める。
							}))
					.entrySet().stream()
					.sorted((o1, o2) -> {
						double v1 = o1.getValue().doubleValue();
						double v2 = o2.getValue().doubleValue();
						return v1 - v2 < 0.0 ? 1 : -1; // スコアの降順で並び替える
					})
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							Map.Entry::getValue,
							(a, b) -> a,
							LinkedHashMap::new)); // 並び順を維持してMapに変換する。
	}

	/**
	 * 相手にこうげきしたときのタイプ相性を示すメッセージのリストを取得する。
	 *
	 * @param bp
	 * @return
	 */
	private List<String> getAtkMsgList(BattlePattern bp) {

		// 自分がこうげきをしたときのこうげきパターン
		final Stream<AttackPattern> atkPtnStream = Stream.of(
				new AttackPattern(bp.getOwnType().getType1(), bp.getOppType()),
				new AttackPattern(bp.getOwnType().getType2(), bp.getOppType())
				);

		return getMsgList(atkPtnStream);
	}

	/**
	 * 相手からこうげきを受けたときのタイプ相性を示すメッセージのリストを取得する。
	 *
	 * @param bp
	 * @return
	 */
	private List<String> getDefMsgList(BattlePattern bp) {

		// 自分がこうげきをしたときのこうげきパターン
		final Stream<AttackPattern> atkPtnStream = Stream.of(
				new AttackPattern(bp.getOppType().getType1(), bp.getOwnType()),
				new AttackPattern(bp.getOppType().getType2(), bp.getOwnType())
				);

		return getMsgList(atkPtnStream);

	}

	/**
	 * 攻撃パターンから、相性を示すメッセージのリストを返却する。
	 *
	 * @param atkPtnStream
	 * @return
	 */
	private List<String> getMsgList(Stream<AttackPattern> atkPtnStream) {

		final Map<AttackPattern, Optional<TypeEffectiveEnum>> tmpMap = atkPtnStream
				.filter(ap -> ap.getAtkType() != null) // こうげき側がnullの場合を省く
				.collect(Collectors.toMap(
						ap -> ap,
						ap -> typeChartInfo.getEffective(ap.getAtkType(), ap.getDefType()), // TypeEffectiveEnumを取得する
						(a, b) -> a, // 重複時の考慮は特になし
						LinkedHashMap::new)); // 順番を担保したいため、LinkedHashMap

		List<String> msgList = tmpMap.entrySet().stream()
				.filter(entry2 -> entry2.getValue().get() != TypeEffectiveEnum.NORMAL) // ×1倍は省く;
				.map(entry2 -> {
					return MessageFormat.format(
							"{0}→{1}（×{2}）",
							entry2.getKey().getAtkType().getJpn(),
							entry2.getKey().getDefType().toJpnString(),
							entry2.getValue().get().getDamageMultiplier());
				})
				.collect(Collectors.toList());

		return msgList;
	}

	/**
	 * 1vs1で戦う際のタイプのパターンを表現するクラス
	 *
	 * @author saibabanagchampa
	 *
	 */
	@Data
	@AllArgsConstructor
	private class BattlePattern {
		TwoTypeKey ownType;
		TwoTypeKey oppType;
	}

	/**
	 * こうげきする彩のパターンを表現するクラス
	 *
	 * @author saibabanagchampa
	 *
	 */
	@Data
	@AllArgsConstructor
	private class AttackPattern {
		TypeEnum atkType;
		TwoTypeKey defType;
	}

	private LinkedHashSet<String> getCommentsMap(
			Optional<String> ownType1Op,
			Optional<String> ownType2Op,
			Optional<String> oppType1Op,
			Optional<String> oppType2Op) {

		// Xがどこに仮定されているか
		final int xTypePosition = getXTypePosition(ownType1Op, ownType2Op, oppType1Op, oppType2Op);
		// Xを定義したのが、こうげき側であるかどうか。
		final boolean isXAtk = xTypePosition == OWN_TYPE1 || xTypePosition == OWN_TYPE2;

		TypeEnum type1 = null;
		TypeEnum type2 = null;
		if (isXAtk) {
			type1 = oppType1Op.isPresent() ? TypeEnum.valueOf(oppType1Op.get()) : null;
			type2 = oppType2Op.isPresent() ? TypeEnum.valueOf(oppType2Op.get()) : null;
		} else {
			type1 = ownType1Op.isPresent() ? TypeEnum.valueOf(ownType1Op.get()) : null;
			type2 = ownType2Op.isPresent() ? TypeEnum.valueOf(ownType2Op.get()) : null;
		}

		final LinkedHashSet<String> comments = typeCommentMap.get(new TwoTypeKey(type1, type2));

		return comments;
	}
}
