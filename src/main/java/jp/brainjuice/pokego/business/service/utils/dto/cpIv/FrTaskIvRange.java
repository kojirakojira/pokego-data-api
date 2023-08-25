package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

/**
 * フィールドリサーチタスク個体値の振れ幅
 *
 * @author saibabanagchampa
 *
 */
public class FrTaskIvRange extends IvRange {

	public FrTaskIvRange() {
		// PLは15固定。天候ブーストの影響なし
		super("15", "15", "15", "15", 10, 10, 15, 15);
	}
}
