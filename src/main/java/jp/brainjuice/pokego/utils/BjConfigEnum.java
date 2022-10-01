package jp.brainjuice.pokego.utils;

/**
 * BjConfigMapのキー名を定義します。
 *
 * @author saibabanagchampa
 *
 */
public class BjConfigEnum {

	public enum Es {
		/** Elasticsearchにおけるワンピースセリフインデックスのドキュメントのidの接頭辞 */
		one_piece_line_index,
	}

	public enum admin {
		/** 権限管理ファイルにおける管理者リスト */
		admin_list;
	}
}
