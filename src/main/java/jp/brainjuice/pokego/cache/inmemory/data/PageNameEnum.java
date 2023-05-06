package jp.brainjuice.pokego.cache.inmemory.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ページ（検索パターン）を列挙型で定義する。
 *
 * @author saibabanagchampa
 *
 */
@AllArgsConstructor
public enum PageNameEnum {

	// jpnを空文字にした場合、ページの閲覧数をカウントしない。

	abundance("ポケモン情報"),
	filterAll("ポケモン検索"),
	searchAll("全ポケ絞り込み"),
	raid("レイドボスCP"),
	shadow("シャドウCP"),
	fRTask("フィールドリサーチCP"),
	eggs("タマゴCP"),
	scpRank("PvP順位"),
	scpRankList("PvP順位一覧"),
	scpRankMaxMin("PvP最高(最低)順位"),
	afterEvoScpRank("進化後PvP順位"),
	afterEvoCp("進化後CP"),
	race("種族値検索"),
	raceDiff("種族値比較"),
	typeScore("タイプ評価"),
	xType("Xタイプ検索"),
	iroiroTypeRank("色々タイプランキング"),
	plList("PLごとのCP一覧"),
	unimplPokemon("未実装ポケモン一覧"),
	evolution("進化ツリーと別のすがた"),;

	@Getter
	private final String jpn;

}
