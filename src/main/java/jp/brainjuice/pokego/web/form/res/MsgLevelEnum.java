package jp.brainjuice.pokego.web.form.res;

/**
 * メッセージのレベルを定義します。
 *
 * @author saibabanagchampa
 *
 */
public enum MsgLevelEnum {
	/**
	 * 警告（期待した情報を返せていない可能性が高いが、情報を補完して結果の返却が可能な場合のメッセージ。）
	 */
	warn,
	/**
	 * エラー（期待した情報を返せない場合のメッセージ。）
	 */
	error,
	/** 情報(デフォルト設定とする。) */
	info,
}
