package jp.brainjuice.pokego.web.form.res.sub;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.web.form.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PrevNextPokemonResponse extends Response {

	private GoPokedex prev;
	private GoPokedex next;
}
