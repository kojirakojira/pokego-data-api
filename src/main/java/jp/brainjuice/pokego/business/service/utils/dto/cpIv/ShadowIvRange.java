package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

/**
 * シャドウ（レイドでなく、ロケット団を倒した後のやつ。）個体値の振れ幅
 *
 * @author saibabanagchampa
 *
 */
public class ShadowIvRange extends IvRange {

	public ShadowIvRange() {
		// シャドウ（レイドでなく、ロケット団を倒した後のやつ。）の場合、PLは、通常時8。天候ブースト時13。
		// 個体値は天候ブースト関係なく0～15。
		super("8", "13", "8", "13", 0, 0, 15, 15);
	}
}
