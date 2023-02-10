package jp.brainjuice.pokego.business.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Type {

	@AllArgsConstructor
	public enum TypeEnum {
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

		public static TypeEnum getType(String str) {
			for(TypeEnum v : values()) {
				if(v.getJpn().equals(str)) {
					return v;
				}
			}
			return null;
		}
	}

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
