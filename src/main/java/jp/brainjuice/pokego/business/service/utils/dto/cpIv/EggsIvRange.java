package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

/**
 * タマゴ個体値の振れ幅
 *
 * @author saibabanagchampa
 *
 */
public class EggsIvRange extends IvRange {

	public EggsIvRange() {
		// PLは15固定。天候ブーストの影響なし
		super("15", null, "15", null, 10, null, 15, null);
	}
}
