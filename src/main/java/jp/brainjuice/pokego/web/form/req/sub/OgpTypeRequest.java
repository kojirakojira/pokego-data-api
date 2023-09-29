package jp.brainjuice.pokego.web.form.req.sub;

import lombok.Data;

@Data
public class OgpTypeRequest {

	// pikedexIdか、type1とtype2か、どちらか一方が必須。
	/** pokedexId */
	private String id;

	private String type1;
	private String type2;
}
