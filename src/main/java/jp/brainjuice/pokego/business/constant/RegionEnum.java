package jp.brainjuice.pokego.business.constant;

import lombok.Getter;

/**
 * 地域を表す列挙型。
 * pokedexIdの5桁目（亜種コード）をvalueにもつ。
 * ※メガ進化(M)はnoneとして扱うようにする。
 *
 * @author saibabanagchampa
 *
 */
public enum RegionEnum {

	none("N", ""), // N（Mもnoneとして扱う。）
	galar("G", "ガラル"), // G
	alola("A", "アローラ"), // A
	hisui("H", "ヒスイ"); // H

	@Getter
	private final String code;
	@Getter
	private final String jpn;

	RegionEnum(String code, String jpn) {
		this.code = code;
		this.jpn = jpn;
	}


	public static RegionEnum getEnumName(String code) {
		for(RegionEnum v : values()) {
			if(v.getCode().equals(code)) {
				return v;
			}
		}
		return "M".equals(code) ? RegionEnum.none: null;
	}
}
