package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

/**
 * ロケット団勝利ボーナス（サカキ）個体値の振れ幅
 *
 * @author saibabanagchampa
 *
 */
public class RocketSakakiIvRange extends IvRange {

	public RocketSakakiIvRange() {
		// ロケット団勝利ボーナス（サカキ）の場合、PLは、通常時8。天候ブースト時13。
		// 個体値は天候ブースト関係なく6～15。
		super("8", "13", "8", "13", 6, 6, 15, 15);
	}
}
