package jp.brainjuice.pokego.web.search.form.res.sub;

import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PrevNextPokemonResponse extends Response {

	private GoPokedex prev;
	private GoPokedex next;
}
