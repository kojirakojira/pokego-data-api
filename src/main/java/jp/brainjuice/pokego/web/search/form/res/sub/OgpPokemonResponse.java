package jp.brainjuice.pokego.web.search.form.res.sub;

import lombok.Data;

/**
 * OGPに使用するためのクラス。
 * ポケモン名と画像を表現する。
 *
 * @author saibabanagchampa
 */
@Data
public class OgpPokemonResponse {

	private String name;
	private String image;
}
