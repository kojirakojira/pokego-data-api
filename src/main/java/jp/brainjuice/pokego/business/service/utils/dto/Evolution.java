package jp.brainjuice.pokego.business.service.utils.dto;

import lombok.Data;

@Data
public class Evolution {

	/** 図鑑ID */
	private String pokedexId;
	/** 進化前の図鑑ID */
	private String beforePokedexId;
	/** 進化に必要なアメの個数 */
	private int candy;
	/** 進化に必要な進化アイテム */
	private String evolutionItems;
	/** 進化に必要な相棒としてのアクション */
	private String buddy;
	/** 特殊な行動、特殊な条件 */
	private String specialAction;
	/** ルアーモジュールを使用した進化条件 */
	private String lureModules;
	/** 進化に必要な交換の条件 */
	private String tradeEvolution;
	/**
	 * 進化方法の注釈
	 * ※進化ツリー全体に対して説明したい条件がある場合に使用する。（ランダムの場合等）
	 */
	private String evoAnnotations;
	/** ポケモンGOで進化するかどうか */
	private boolean canGoEvo;

	/**
	 * ポケモンGOで実装済みであるか
	 * ※pokemon-evolution.csvではなく、pokemon.csvから情報を取得する項目。
	 */
	private boolean implFlg = false;
}
