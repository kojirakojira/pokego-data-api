package jp.brainjuice.pokego.business.service.search.pokeFilter;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ポケモン絞り込み機能で絞り込みに使用できるキーワード
 *
 * @author saibabanagchampa
 *
 */
@AllArgsConstructor
public enum FilterEnum {
	/**
	 * タイプ<br>
	 * String or TypeEnum or List<String><br>
	 * List検索はor演算
	 * 
	 * @see TypeEnum
	 */
	type("タイプ"),
	/**
	 * 2タイプ<br>
	 * List&lt;String&gt;(2elements)
	 * 
	 * @see TypeEnum
	 */
	twoType("タイプ"),
	/**
	 * 最終進化<br>
	 * Boolean<br>
	 * ※trueの場合のみ絞り込む。falseの場合は絞り込みは実施しない。
	 */
	finEvo("最終進化"),
	/**
	 * メガシンカ<br>
	 * Boolean<br>
	 * ※trueの場合のみ絞り込む。falseの場合は絞り込みは実施しない。
	 */
	mega("メガシンカ"),
	/**
	 * ダイマックス<br>
	 * Boolean<br>
	 * ※trueの場合のみ絞り込む。falseの場合は絞り込みは実施しない。
	 */
	dynamax("ダイマックス"),
	/**
	 * キョダイマックス<br>
	 * Boolean<br>
	 * ※trueの場合のみ絞り込む。falseの場合は絞り込みは実施しない。
	 */
	gigantamax("キョダイマックス"),
	/**
	 * 実装済み<br>
	 * Boolean<br>
	 * ※trueの場合のみ絞り込む。falseの場合は絞り込みは実施しない。
	 */
	impled("PokémonGO実装済み"),
	/**
	 * リリース年月（開始）
	 */
	releaseDateStart("PokémonGOリリース年月"),
	/**
	 * リリース年月（終了）
	 */
	releaseDateEnd("PokémonGOリリース年月"),
	/**
	 * 強ポケ補正<br>
	 * Boolean<br>
	 * ※trueの場合のみ絞り込む。falseの場合は絞り込みは実施しない。
	 */
	tooStrong("強ポケ補正"),
	/**
	 * 地域<br>
	 * String or RegionEnum or List<String><br>
	 * List検索はor演算
	 * 
	 * @see RegionEnum
	 */
	region("地域"),
	/**
	 * 世代<br>
	 * String or GenNameEnum or List<String><br>
	 * List検索はor演算
	 * 
	 * @see GenNameEnum
	 */
	gen("世代"),
	;

	@Getter
	private String jpn;

}
