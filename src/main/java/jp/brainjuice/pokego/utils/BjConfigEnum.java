package jp.brainjuice.pokego.utils;

/**
 * BjConfigMapのキー名を定義します。
 *
 * @author saibabanagchampa
 *
 */
public class BjConfigEnum {

	public enum System {
		SPRING_PROFILES_ACTIVE /** 環境変数 **/
	}

	public enum admin {
		/** 権限管理ファイルにおける管理者リスト */
		admin_list;
	}
}
