package jp.brainjuice.pokego.web.form.res.sub;

import lombok.Data;

/**
 * OGPに使用するためのクラス。
 * タイプを表現する。
 *
 * @author saibabanagchampa
 */
@Data
public class OgpTypeResponse {

	// 2タイプの場合、タイプ1とタイプ2をカンマ区切りで連結させた文字列にする。
	private String type;
}
