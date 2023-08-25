package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

/**
 * 全個体値の振れ幅
 *
 * @author saibabanagchampa
 *
 */
public class AllIvRange extends IvRange {

	public AllIvRange() {
		// PLは1～51。天候ブーストの概念は当然なし。
		super("1", "51", "1", "51", 0, 0, 15, 15);
	}
}
