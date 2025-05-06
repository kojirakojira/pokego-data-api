package jp.brainjuice.pokego.business.service.search.utils.dto.cpIv;

public class DynamaxIvRange extends IvRange {

	public DynamaxIvRange() {
		// ダイマックス、キョダイマックスの場合、PLは20。天候ブーストの影響なし。
		// 個体値は天候ブースト関係なく10～15。
		super("20", null, "20", null, 10, null, 15, null);
	}
}
