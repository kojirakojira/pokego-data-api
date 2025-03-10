package jp.brainjuice.pokego.business.service.utils.dto.type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import jp.brainjuice.pokego.business.constant.Type.TypeEffectiveEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class TypeStrength {

	private String type;
	private double normal;
	private double fire;
	private double water;
	private double grass;
	private double electric;
	private double ice;
	private double fighting;
	private double poison;
	private double ground;
	private double flying;
	private double psychic;
	private double bug;
	private double rock;
	private double ghost;
	private double dragon;
	private double dark;
	private double steel;
	private double fairy;

	/**
	 * タイプの倍率を保持したMapを取得する。<br>
	 * ※性能を重視した実装にしている。
	 *
	 * @return
	 */
	public Map<TypeEnum, TypeEffectiveEnum> mapping() {
		
		// Map<ダメージ倍率, TypeEffectiveEnum>
		Map<Double, TypeEffectiveEnum> effectiveMap = Map.ofEntries(
				Map.entry(Double.valueOf(TypeEffectiveEnum.HIGH.getDamageMultiplier()), TypeEffectiveEnum.HIGH),
				Map.entry(Double.valueOf(TypeEffectiveEnum.NORMAL.getDamageMultiplier()), TypeEffectiveEnum.NORMAL),
				Map.entry(Double.valueOf(TypeEffectiveEnum.LOW.getDamageMultiplier()), TypeEffectiveEnum.LOW),
				Map.entry(Double.valueOf(TypeEffectiveEnum.VERY_LOW.getDamageMultiplier()), TypeEffectiveEnum.VERY_LOW)
				);

		return Map.ofEntries(
				Map.entry(TypeEnum.normal, effectiveMap.get(getNormal())),
				Map.entry(TypeEnum.fire, effectiveMap.get(getFire())),
				Map.entry(TypeEnum.water, effectiveMap.get(getWater())),
				Map.entry(TypeEnum.grass, effectiveMap.get(getGrass())),
				Map.entry(TypeEnum.electric, effectiveMap.get(getElectric())),
				Map.entry(TypeEnum.ice, effectiveMap.get(getIce())),
				Map.entry(TypeEnum.fighting, effectiveMap.get(getFighting())),
				Map.entry(TypeEnum.poison, effectiveMap.get(getPoison())),
				Map.entry(TypeEnum.ground, effectiveMap.get(getGround())),
				Map.entry(TypeEnum.flying, effectiveMap.get(getFlying())),
				Map.entry(TypeEnum.psychic, effectiveMap.get(getPsychic())),
				Map.entry(TypeEnum.bug, effectiveMap.get(getBug())),
				Map.entry(TypeEnum.rock, effectiveMap.get(getRock())),
				Map.entry(TypeEnum.ghost, effectiveMap.get(getGhost())),
				Map.entry(TypeEnum.dragon, effectiveMap.get(getDragon())),
				Map.entry(TypeEnum.dark, effectiveMap.get(getDark())),
				Map.entry(TypeEnum.steel, effectiveMap.get(getSteel())),
				Map.entry(TypeEnum.fairy, effectiveMap.get(getFairy()))
				);
	}

	/**
	 * リフレクションを使用し、タイプからgetterにアクセスする。
	 *
	 * @param type
	 * @return
	 */
	public double get(TypeEnum type) {

		double f = 0f;
		try {
			final Method method = TypeStrength.class.getMethod("get" + StringUtils.capitalize(type.name()));
			f = (double) method.invoke(this);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// コーディングミスをしない限りエラーは発生しない。
			log.error(type.name());
		}
		return f;
	}

	/**
	 * リフレクションを使用し、タイプからsetterにアクセスする。
	 *
	 * @param type
	 * @param value
	 */
	public void set(TypeEnum type, double value) {

		try {
			final Method method = TypeStrength.class.getMethod("set" + StringUtils.capitalize(type.name()), double.class);
			method.invoke(this, value);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// コーディングミスをしない限りエラーは発生しない。
			log.error(type.name());
		}
	}
}
