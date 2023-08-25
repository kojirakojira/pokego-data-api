package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

/**
 * 野生個体値の振れ幅
 *
 * @author saibabanagchampa
 *
 */
public class WildIvRange extends IvRange {

	public WildIvRange() {
		// 野生のPLは1～30。天候ブースト時は6～35
		// 個体値は最低保証なしの0～15。天候ブースト時は最低保証ありの4～15
		super("1", "6", "30", "35", 0, 4, 15, 15);
	}
}
