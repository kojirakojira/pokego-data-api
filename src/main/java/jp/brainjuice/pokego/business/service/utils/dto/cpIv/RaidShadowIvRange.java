package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

/**
 * レイドボス（シャドウ）個体値の振れ幅
 *
 * @author saibabanagchampa
 *
 */
public class RaidShadowIvRange extends IvRange {

	public RaidShadowIvRange() {
		// レイドボス（シャドウ）の場合、PLは、通常時20。天候ブースト時25。
		// 個体値は天候ブースト関係なく6～15。
		super("20", "25", "20", "25", 6, 6, 15, 15);
	}
}
