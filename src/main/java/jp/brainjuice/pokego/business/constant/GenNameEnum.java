package jp.brainjuice.pokego.business.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum GenNameEnum implements ConstantEnum {
	g1("第一世代(赤緑青)"),
	g2("第二世代(金銀)"),
	g3("第三世代(ルビーサファイア)"),
	g4("第四世代(ダイアモンドパール)"),
	g5("第五世代(ブラックホワイト)"),
	g6("第六世代(XY)"),
	g6orac("第六世代(ORAC)"),
	g7("第七世代(サンムーン)"),
	g7pv("第七世代(Let's GO! ピカチュウ&イーブイ)"),
	g8("第八世代(剣盾)"),
	g8arceus("LEGENDS アルセウス"),
	;

	@Getter
	private final String jpn;

	public static GenNameEnum getEnumName(String str) {
		for(GenNameEnum v : values()) {
			if(v.getJpn().equals(str)) {
				return v;
			}
		}
		return null;
	}
}
