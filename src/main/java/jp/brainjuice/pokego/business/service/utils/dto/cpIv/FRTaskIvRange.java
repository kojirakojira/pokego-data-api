package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

/**
 * フィールドリサーチタスク個体値の振れ幅
 *
 * @author saibabanagchampa
 *
 */
public class FRTaskIvRange extends IvRange {

	public FRTaskIvRange() {
		// PLは15固定。天候ブーストの影響なし
		super("15", null, "15", null, 10, null, 15, null);
	}
}
