package jp.brainjuice.pokego.web.form.res;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PrevNextPokemonResponse extends Response {

	private GoPokedex prev;
	private GoPokedex next;
}
