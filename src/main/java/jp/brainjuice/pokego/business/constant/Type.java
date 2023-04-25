package jp.brainjuice.pokego.business.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Type {

	/**
	 * タイプを定義する。
	 *
	 * @author saibabanagchampa
	 *
	 */
	@AllArgsConstructor
	public enum TypeEnum implements ConstantEnumInterface {
		/** ノーマル */
		normal("ノーマル"),
		/** ほのお */
		fire("ほのお"),
		/** みず */
		water("みず"),
		/** くさ */
		grass("くさ"),
		/** でんき */
		electric("でんき"),
		/** こおり */
		ice("こおり"),
		/** かくとう */
		fighting("かくとう"),
		/** どく */
		poison("どく"),
		/** じめん */
		ground("じめん"),
		/** ひこう */
		flying("ひこう"),
		/** エスパー */
		psychic("エスパー"),
		/** むし */
		bug("むし"),
		/** いわ */
		rock("いわ"),
		/** ゴースト */
		ghost("ゴースト"),
		/** ドラゴン */
		dragon("ドラゴン"),
		/** あく */
		dark("あく"),
		/** はがね */
		steel("はがね"),
		/** フェアリー */
		fairy("フェアリー");

		@Getter
		private final String jpn;

		/**
		 * 日本語名からTypeEnumを取得する。
		 *
		 * @param jpn
		 * @return
		 */
		public static TypeEnum getType(String jpn) {
			for(TypeEnum v : values()) {
				if(v.getJpn().equals(jpn)) {
					return v;
				}
			}
			return null;
		}

		/**
		 * 引数に指定された文字列が、TypeEnumに定義されているかを判定する。
		 *
		 * @param type
		 * @return
		 */
		public static boolean isDefined(String type) {

			boolean flg = false;

			if (type == null) return flg;

			for (TypeEnum te: TypeEnum.values()) {
				if (te.name().equals(type)) {
					flg = true;
					break;
				}
			}

			return flg;
		}
	}

	/**
	 * タイプのダメージ倍率を定義する。
	 *
	 * @author saibabanagchampa
	 *
	 */
	@AllArgsConstructor
	public enum TypeEffectiveEnum {
		/**
		 * 効果はばつぐんだ！<br>
		 * <ul>
		 * <li>抜群×抜群</li>
		 * </ul><br>
		 * (原作×4, GO×2.56)
		 */
		MAX(2.56d),
		/**
		 * 効果はばつぐんだ！<br>
		 * <ul>
		 * <li>抜群</li>
		 * <li>抜群×普通</li>
		 * </ul><br>
		 * GO×1.6(原作×2)
		 */
		HIGH(1.6d),
		/**
		 * (普通の効果)<br>
		 * <ul>
		 * <li>普通</li>
		 * <li>半減×抜群</li>
		 * </ul><br>
		 * GO×1(原作×1)
		 */
		NORMAL(1),
		/**
		 * 効果はいまひとつだ...<br>
		 * <ul>
		 * <li>耐性</li>
		 * <li>耐性×普通</li>
		 * <li>無効×抜群</li>
		 * </ul><br>
		 * GO×0.625(原作×0.5 or 0)
		 */
		LOW(0.625d),
		/**
		 * 効果がないようだ...<br>
		 * <ul>
		 * <li>無効</li>
		 * <li>無効×普通</li>
		 * <li>耐性×耐性</li>
		 * </ul><br>
		 * GO×0.390625(原作×0 or 0.25)
		 */
		VERY_LOW(0.390625d),
		/**
		 * 効果がないようだ...<br>
		 * <ul>
		 * <li>耐性×無効</li>
		 * </ul>
		 * GO×0.244140625(原作×0)
		 * */
		MIN(0.244140625d),
		;
		/** ダメージ倍率 */
		@Getter
		private double damageMultiplier;
	}

	/**
	 * タイプの色を定義する。
	 *
	 * @author saibabanagchampa
	 *
	 */
	@AllArgsConstructor
	public enum TypeColorEnum {
		/** ノーマル */
		normal(144, 153, 161),
		/** ほのお */
		fire(255, 156, 84),
		/** みず */
		water(77, 144, 213),
		/** くさ */
		grass(99, 187, 91),
		/** でんき */
		electric(243, 210, 59),
		/** こおり */
		ice(116, 206, 192),
		/** かくとう */
		fighting(206, 64, 105),
		/** どく */
		poison(171, 106, 200),
		/** じめん */
		ground(217, 119, 70),
		/** ひこう */
		flying(143, 168, 221),
		/** エスパー */
		psychic(249, 113, 118),
		/** むし */
		bug(144, 193, 44),
		/** いわ */
		rock(199, 183, 139),
		/** ゴースト */
		ghost(82, 105, 172),
		/** ドラゴン */
		dragon(10, 109, 196),
		/** あく */
		dark(90, 83, 102),
		/** はがね */
		steel(90, 142, 161),
		/** フェアリー */
		fairy(236, 143, 230),
		;

		@Getter
		private final int r;
		@Getter
		private final int g;
		@Getter
		private final int b;

		public static TypeColorEnum getTypeColorForJpn(String jpn) {
			return TypeColorEnum.valueOf((TypeEnum.getType(jpn)).name());
		}

	}
}
