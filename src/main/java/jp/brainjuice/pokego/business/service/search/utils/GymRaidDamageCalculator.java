package jp.brainjuice.pokego.business.service.search.utils;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.constant.Type.TypeEffectiveEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.constant.WeatherBoosts;
import jp.brainjuice.pokego.business.constant.WeatherBoosts.WeatherEnum;
import jp.brainjuice.pokego.business.service.search.utils.dto.damage.GymRaidAttackScoreInDto;
import jp.brainjuice.pokego.business.service.search.utils.dto.damage.GymRaidAttackScoreOutDto;
import jp.brainjuice.pokego.business.service.search.utils.dto.damage.GymRaidTenMinAttackCounts;
import jp.brainjuice.pokego.business.service.search.utils.dto.type.TwoTypeKey;
import jp.brainjuice.pokego.cache.inmemory.TypeChartInfo;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;

@Component
public class GymRaidDamageCalculator {

	private TypeChartInfo typeChartInfo;

	private WeatherBoosts weatherBoosts;

	/** 1回のレイドの最長時間(星4以上)。 */
	private final double TEN_MIN_SEC = 5.0 * 60.0;

	private final double ONE_MULTIPLIER = 1D;

	private final double SAME_TYPE_MULTIPLIER = 1.2D;

	private final double WEATHER_BOOSTS_MULTIPLIER = 1.2D;

	private final double SHADOW_MULTIPLIER = 1.2D;

	/** ぼうぎょ側のぼうぎょ力の固定値 */
	private final double DEFENSE_FIXED_VALUE = 100D;

	GymRaidDamageCalculator(TypeChartInfo typeChartInfo, WeatherBoosts weatherBoosts) {
		this.typeChartInfo = typeChartInfo;
		this.weatherBoosts = weatherBoosts;
	}

	/**
	 * 通常技、スペシャル技の組み合わせのスコアを求める。<br>
	 * 3分間に与えられるダメージ量をスコアとする。
	 *
	 * @param fa
	 * @param ca
	 * @param attackerType
	 * @param defenderType
	 * @param weather 天候ブーストを考慮しない場合はnullを設定する
	 * @return
	 */
	public GymRaidAttackScoreOutDto attackScore(GymRaidAttackScoreInDto in) {

		GoPokedex gp = in.getGoPokedex();
		FastAttack fa = in.getFastAttack();
		ChargedAttack ca = in.getChargedAttack();
		TwoTypeKey attackerType = new TwoTypeKey(gp.getType1(), gp.getType2());
		TwoTypeKey defenderType = in.getDefenderType();
		WeatherEnum weather = in.getWeather();
		boolean isShadow = in.isShadow();

		GymRaidTenMinAttackCounts counts = calcTenMinAttackCounts(fa, ca);

		double faDamageScore;
		{
			TypeEnum type = fa.getType();
			double damageCorrection = getDamageCorrection(type, attackerType, defenderType, weather, isShadow);

			double attackerAttack = (double) gp.getAttack(); // こうげき側のこうげき力
			double damage = (0.5D * fa.getGymPower() * (attackerAttack / DEFENSE_FIXED_VALUE) * damageCorrection) + 1;
			faDamageScore = counts.getFastAttackCount() * damage;
		}

		double caDamageScore;
		{
			TypeEnum type = ca.getType();
			double damageCorrection = getDamageCorrection(type, attackerType, defenderType, weather, isShadow);

			double attackerAttack = (double) gp.getAttack(); // こうげき側のこうげき力
			double damage = (0.5D * ca.getGymPower() * (attackerAttack / DEFENSE_FIXED_VALUE) * damageCorrection) + 1;
			caDamageScore = counts.getChargedAttackCount() * damage;
		}

		GymRaidAttackScoreOutDto out = new GymRaidAttackScoreOutDto();
		double faScore = Math.round(faDamageScore * 10D) / 10D;
		double caScore = Math.round(caDamageScore * 10D) / 10D;
		out.setFastAttackScore(faScore);
		out.setChargedAttackScore(caScore);
		out.setAttackScore(Math.round((faScore + caScore) * 10D) / 10D);

		return out;
	}

	/**
	 * 5分間に撃てる通常技、スペシャル技の回数を求める。
	 * @param fa
	 * @param ca
	 * @return
	 */
	public GymRaidTenMinAttackCounts calcTenMinAttackCounts(FastAttack fa, ChargedAttack ca) {

		double faTotalSec = fa.getTotalMs() / 1000.0D;
		double caTotalSec = ca.getTotalMs() / 1000.0D;
		double faDamageSec = fa.getDamageMs() / 1000.0D;

		// スペシャル技を打つのに必要な通常技の回数
		// ※スペシャル技のゲージ増加量はマイナス値
		// ※スペシャル技を撃った後の余りを考慮するため、端数の切り上げはしない。
		double caRequirementCnt = (0.0D - ca.getGymEnergyIncrAmount()) / fa.getGymEnergyIncrAmount();

		// スペシャル技を撃ち終わるまでの時間を1セットと考え、その1セットの時間
		double oneSetSec = caRequirementCnt * faTotalSec + caTotalSec;

		// 5分間に撃てるスペシャル技の回数
		double maxCaUseCnt = Math.floor(TEN_MIN_SEC / oneSetSec);

		// 5分間に撃てるスペシャル技を溜めるのに必要な通常技の回数
		double maxCaRequirementFaCnt = caRequirementCnt * maxCaUseCnt;

		// 5分間にスペシャル技を繰り返し、余った時間(秒)
		double remainderSec = TEN_MIN_SEC - maxCaUseCnt * oneSetSec;

		// 5分間にスペシャル技を繰り返し、余った時間に撃てる通常技の回数
		double remainderFaCnt = Math.floor(remainderSec / faTotalSec);
		// 通常技を撃ってさらに時間が余った場合、通常技のダメージが発生するまでの時間を見て発動可否を判定する必要がある。
		remainderFaCnt = (remainderSec - faTotalSec * remainderFaCnt) > faDamageSec ? remainderFaCnt + 1 : remainderFaCnt;

		// 通常技発動回数
		double faCnt = Math.floor(maxCaRequirementFaCnt) + remainderFaCnt;

		GymRaidTenMinAttackCounts tenMinAttackCnts = new GymRaidTenMinAttackCounts();
		tenMinAttackCnts.setFastAttackCount(faCnt);
		tenMinAttackCnts.setChargedAttackCount(maxCaUseCnt);
		return tenMinAttackCnts;
	}

	private double getDamageCorrection(TypeEnum attackType, TwoTypeKey attackerType, TwoTypeKey defenderType, WeatherEnum weather, boolean isShadow) {

		double typeEffectiveness = ONE_MULTIPLIER;
		if (defenderType != null) {
			TypeEffectiveEnum effective = typeChartInfo.getEffective(attackType, defenderType).orElseThrow();
			typeEffectiveness = effective.getDamageMultiplier();
		}
		// タイプ一致ボーナス
		double sameTypeBonus = ONE_MULTIPLIER;
		if (attackType != null && (attackType == attackerType.getType1() || attackType == attackerType.getType2())) {
			sameTypeBonus = SAME_TYPE_MULTIPLIER;
		}
		// 天候ブースト倍率
		double wbMultiplier = ONE_MULTIPLIER;
		if (weather != null && weatherBoosts.getTypeWbLookupMap().get(weather).contains(attackType)) {
			wbMultiplier = WEATHER_BOOSTS_MULTIPLIER;
		}
		// シャドウ倍率
		double shadowMultiplier = isShadow ? SHADOW_MULTIPLIER : ONE_MULTIPLIER;

		// 仲良し度、よける倍率は考慮しない。

		return typeEffectiveness * sameTypeBonus * wbMultiplier * shadowMultiplier;
	}

}
