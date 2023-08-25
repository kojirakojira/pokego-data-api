package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

/**
 * シャドウ（サカキを倒した後のやつ。レイドでない。）個体値の振れ幅
 *
 * @author saibabanagchampa
 *
 */
public class ShadowSakakiIvRange extends IvRange {

	public ShadowSakakiIvRange() {
		// シャドウ（サカキを倒した後のやつ。レイドでない。）の場合、PLは、通常時8。天候ブースト時13。
		// 個体値は天候ブースト関係なく6～15。
		super("8", "13", "8", "13", 6, 6, 15, 15);
	}
}
