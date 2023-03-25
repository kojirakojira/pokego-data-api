package jp.brainjuice.pokego.business.service.utils.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.constant.Type;
import jp.brainjuice.pokego.business.constant.Type.TypeEffectiveEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.utils.dto.TypeStrength;
import jp.brainjuice.pokego.utils.BjCsvMapper;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author saibabanagchampa
 *
 */
@Component
@Slf4j
public class TypeChartInfo {

	/** TypeStrengthのtypeには、攻撃する側のタイプを持つ */
	private Map<TypeEnum, TypeStrength> typeChartMap = new HashMap<>();

	/** 相性表のファイル名 */
	private static final String FILE_NAME = "config/type-chart.csv";

	// 最大、最小の攻撃する側のスコア
	private double maxAfScore;
	private double minAfScore;

	// 最大、最小の攻撃を受ける側のスコア
	private double maxDfScore;
	private double minDfScore;

	/**
	 * 引数に指定したタイプの攻撃に対する、倍率ごとのタイプを取得する。
	 * @param type
	 * @return
	 */
	public Map<TypeEffectiveEnum, List<TypeEnum>> getAttackerTypes(TypeEnum type) {

		final Map<TypeEffectiveEnum, List<TypeEnum>> retMap = new HashMap<>();

		for (TypeEffectiveEnum tee: TypeEffectiveEnum.values()) {
			retMap.put(tee, getAttackerTypes(type, tee));
		}

		return retMap;
	}

	/**
	 * 1タイプもつポケモンが攻撃を受けたときタイプを倍率ごとに取得する。
	 *
	 * @param type
	 * @return
	 */
	public Map<TypeEffectiveEnum, List<TypeEnum>> getDefenderTypes(TypeEnum type) {

		final Map<TypeEffectiveEnum, List<TypeEnum>> retMap = new HashMap<>();

		for (TypeEffectiveEnum tee: TypeEffectiveEnum.values()) {
			retMap.put(tee, getDefenderTypes(type, tee));
		}

		return retMap;
	}

	/**
	 * @param type1
	 * @param type2
	 * @return
	 */
	public Map<TypeEffectiveEnum, List<TypeEnum>> getDefenderTypes(TypeEnum type1, TypeEnum type2) {

		if (type1 == null || type2 == null || type1 == type2) {
			// type1かtype2がnull、またはtype1とtype2が一致する場合は、単一タイプとして実行。
			TypeEnum type = type1 == null ? type2 : type1;
			return getDefenderTypes(type);
		}

		final Map<TypeEffectiveEnum, List<TypeEnum>> retMap = new HashMap<>();

		// Mapに0～5のすべて倍率を設定
		for (TypeEffectiveEnum tee: TypeEffectiveEnum.values()) {
			retMap.put(tee, new ArrayList<>());
		}

		Map<TypeEnum, Double> strength1Map = getDefenderTypeStrength(type1).mapping();
		Map<TypeEnum, Double> strength2Map = getDefenderTypeStrength(type2).mapping();

		for (TypeEnum type: TypeEnum.values()) {
			final double effective = strength1Map.get(type).doubleValue() * strength2Map.get(type).doubleValue();

			for (TypeEffectiveEnum tee: TypeEffectiveEnum.values()) {
				if (Math.abs(tee.getDamageMultiplier() - effective) < 0.0001) {
					// (誤差を考慮した上で)倍率が一致するリストに追加する。
					retMap.get(tee).add(type);
					break;
				}
			}

		}

		return retMap;
	}

	/**
	 * 第1引数に指定したタイプに対して、第2引数に指定した効果のタイプを取得する。<br>
	 * 攻撃を与える側における倍率。
	 *
	 * @param type
	 * @param effective the TypeChartInfo field
	 * @return
	 */
	public List<TypeEnum> getAttackerTypes(TypeEnum type, TypeEffectiveEnum effective) {

		final Map<TypeEnum, Double> strengthMap = getAttackerTypeStrength(type).mapping();

		final double damageRate = effective.getDamageMultiplier();

		return strengthMap.entrySet().stream()
				.filter(e -> e.getValue().doubleValue() == damageRate)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	/**
	 * 第1引数に指定したタイプに対して、第2引数に指定した効果のタイプを取得する。<br>
	 * 攻撃を受ける側における倍率。
	 *
	 * @param type
	 * @param effective the TypeChartInfo field
	 * @return
	 */
	public List<TypeEnum> getDefenderTypes(TypeEnum type, TypeEffectiveEnum effective) {

		final Map<TypeEnum, Double> strengthMap = getDefenderTypeStrength(type).mapping();

		final double damageRate = effective.getDamageMultiplier();

		return strengthMap.entrySet().stream()
				.filter(e -> e.getValue().doubleValue() == damageRate)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	/**
	 * 攻撃する側、攻撃を受ける側の両方のスコアの総合点を取得する。
	 *
	 * @param type
	 * @return
	 */
	public double score(TypeEnum type) {
		double score = attackerScore(type) + defenderScore(type);
		return ((double) Math.round(score * 10d)) / 10d;
	}

	/**
	 * 攻撃する側のスコアを取得する。<br>
	 * すべてのタイプへの攻撃倍率からスコアを算出する。
	 *
	 * @param type
	 * @return
	 */
	public double attackerScore(TypeEnum type) {
		double score = (getAttackerPoint(type) - minAfScore) * 5 / (maxAfScore - minAfScore);
		return ((double) Math.round(score * 10d)) / 10d;
	}

	/**
	 * 攻撃を受ける側のスコアを取得する。<br>
	 * すべてのタイプからの攻撃倍率からスコアを算出する。
	 *
	 * @param type
	 * @return
	 */
	public double defenderScore(TypeEnum type) {
		double score = (maxDfScore - getDefenderPoint(type)) * 5 / (maxDfScore - minDfScore);
		return ((double) Math.round(score * 10d)) / 10d;
	}

	/**
	 * 攻撃を受ける側のスコアを取得する。<br>
	 * すべてのタイプからの攻撃倍率からスコアを算出する。
	 *
	 * @param type
	 * @return
	 */
	public double defenderScore(TypeEnum type1, TypeEnum type2) {
		double point;
		if (type1 == null || type2 == null || type1 == type2) {
			// type1かtype2がnull、またはtype1とtype2が一致する場合は、単一タイプとして実行。
			TypeEnum type = type1 == null ? type2 : type1;
			point = getDefenderPoint(type);
		} else {
			point = (getDefenderPoint(type1) + getDefenderPoint(type2)) / 2;
		}
		double score = (maxDfScore - point) * 5 / (maxDfScore - minDfScore);
		return ((double) Math.round(score * 10d)) / 10d;
	}

	/**
	 * 攻撃を与える側における、すべてのタイプとの相性をスコア化する。<br>
	 * スコアが高い方が強い。
	 *
	 * @param type
	 * @return
	 */
	private double getAttackerPoint(TypeEnum type) {
		double score = 0d;
		TypeStrength attackerTypeStrength = getAttackerTypeStrength(type);

		Map<TypeEnum, Double> strengthMap = attackerTypeStrength.mapping();
		for (Map.Entry<TypeEnum, Double> entry: strengthMap.entrySet()) {
			score+=entry.getValue().doubleValue();
		}

		return score;
	}

	/**
	 * 攻撃を受ける側における、すべてのタイプとの相性をスコア化する。<br>
	 * スコアが低い方が強い。
	 *
	 * @param type
	 * @return
	 */
	private double getDefenderPoint(TypeEnum type) {
		double score = 0d;
		TypeStrength defenderTypeStrength = getDefenderTypeStrength(type);

		Map<TypeEnum, Double> strengthMap = defenderTypeStrength.mapping();
		for (Map.Entry<TypeEnum, Double> entry: strengthMap.entrySet()) {
			score+=entry.getValue().doubleValue();
		}

		return score;
	}

	/**
	 * 攻撃を与える側のタイプ相性を取得する。<br>
	 * (type-chart.csvで見て、x軸方向の情報を取得する。)
	 *
	 * @param type
	 * @return
	 */
	public TypeStrength getAttackerTypeStrength(TypeEnum type) {
		return typeChartMap.get(type);
	}

	/**
	 * 攻撃を受ける側のタイプ相性を取得する。<br>
	 * (type-chart.csvで見て、y軸方向の情報を取得する。)
	 *
	 * @param type
	 * @return
	 */
	public TypeStrength getDefenderTypeStrength(TypeEnum type) {

		final TypeStrength typeStrength = new TypeStrength();

		// タイプをセット
		typeStrength.setType(type.name());

		/* typeChartMapの各要素から受ける側の倍率を取得する。 */
		// keyは攻撃する側のタイプ、valueは倍率。
		final Map<TypeEnum, Double> strengthMap = new HashMap<>();
		{

			for (Map.Entry<TypeEnum, TypeStrength> entry: typeChartMap.entrySet()) {

				// 各要素を取得し、putする。
				final double strength = entry.getValue().get(type);
				strengthMap.put(entry.getKey(), Double.valueOf(strength));
			}
		}

		// 返却値用のTypeStrengthのsetterにアクセスし、セットする。
		final Set<Map.Entry<TypeEnum, Double>> entrySet = strengthMap.entrySet();
		for (Map.Entry<TypeEnum, Double> entry: entrySet) {
			typeStrength.set(entry.getKey(), entry.getValue().doubleValue());
		}

		return typeStrength;
	}

	public LinkedHashMap<TypeEnum, Integer> rankAll() {
		return null;
	}

	private double convGoDamageRate(double oriDamageRate) {

		double goDamageRate;

		if (Math.abs(oriDamageRate - 0d) < 0.0001) {
			goDamageRate = Type.TypeEffectiveEnum.VERY_LOW.getDamageMultiplier();
		} else if (Math.abs(oriDamageRate - 0.5d) < 0.0001) {
			goDamageRate = Type.TypeEffectiveEnum.LOW.getDamageMultiplier();
		} else if (Math.abs(oriDamageRate - 1d) < 0.0001) {
			goDamageRate = Type.TypeEffectiveEnum.NORMAL.getDamageMultiplier();
		} else if (Math.abs(oriDamageRate - 2d) < 0.0001) {
			goDamageRate = Type.TypeEffectiveEnum.HIGH.getDamageMultiplier();
		} else {
			throw new IllegalArgumentException("Unexpected value: " + oriDamageRate);
		}

		return goDamageRate;
	}

	/**
	 * 起動時に実行。
	 *
	 * @throws PokemonDataInitException
	 */
	@PostConstruct
	public void init() throws PokemonDataInitException {

		// CSVファイルの内容をメモリに抱える。
		try {
			List<TypeStrength> strengthList = BjCsvMapper.mapping(FILE_NAME, TypeStrength.class);
			strengthList.forEach(e -> {
				for (TypeEnum te: TypeEnum.values()) {
					e.set(te, convGoDamageRate(e.get(te)));
				}
			});
			typeChartMap = strengthList.stream().collect(Collectors.toMap(e -> TypeEnum.valueOf(e.getType()), e -> e));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}

		maxAfScore = 0d;
		minAfScore = 99d;
		for (TypeEnum type: TypeEnum.values()) {
			double score = getAttackerPoint(type);
			if (maxAfScore < score) {
				maxAfScore = score;
			}
			if (minAfScore > score) {
				minAfScore = score;
			}
		}

		maxDfScore = 0d;
		minDfScore = 99d;
		for (TypeEnum type1: TypeEnum.values()) {
			double score1 = getDefenderPoint(type1);
			for (TypeEnum type2: TypeEnum.values()) {
				double score2 = getDefenderPoint(type2);
				double score = type1 == type2 ? score1 : (score1 + score2) / 2;
				if (maxDfScore < score) {
					maxDfScore = score;
				}
				if (minDfScore > score) {
					minDfScore = score;
				}
			}
		}


		log.info("TypeChartInfo generated!!");
	}
}
