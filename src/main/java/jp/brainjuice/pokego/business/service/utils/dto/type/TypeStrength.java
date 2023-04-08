package jp.brainjuice.pokego.business.service.utils.dto.type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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
	public Map<TypeEnum, Double> mapping() {


		final Map<TypeEnum, Double> map = new LinkedHashMap<>(18 * 4 / 3);

		map.put(TypeEnum.normal, getNormal());
		map.put(TypeEnum.fire, getFire());
		map.put(TypeEnum.water, getWater());
		map.put(TypeEnum.grass, getGrass());
		map.put(TypeEnum.electric, getElectric());
		map.put(TypeEnum.ice, getIce());
		map.put(TypeEnum.fighting, getFighting());
		map.put(TypeEnum.poison, getPoison());
		map.put(TypeEnum.ground, getGround());
		map.put(TypeEnum.flying, getFlying());
		map.put(TypeEnum.psychic, getPsychic());
		map.put(TypeEnum.bug, getBug());
		map.put(TypeEnum.rock, getRock());
		map.put(TypeEnum.ghost, getGhost());
		map.put(TypeEnum.dragon, getDragon());
		map.put(TypeEnum.dark, getDark());
		map.put(TypeEnum.steel, getSteel());
		map.put(TypeEnum.fairy, getFairy());

		return map;
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
