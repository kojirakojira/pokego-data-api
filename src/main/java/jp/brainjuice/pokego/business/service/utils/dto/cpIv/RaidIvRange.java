package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

/**
 * レイドボス個体値の振れ幅
 *
 * @author saibabanagchampa
 *
 */
public class RaidIvRange extends IvRange {

	public RaidIvRange() {
		// レイドボスの場合、PLは、通常時20。天候ブースト時25。
		// 個体値は天候ブースト関係なく10～15。
		super("20", "25", "20", "25", 10, 10, 15, 15);
	}
}
