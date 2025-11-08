package jp.brainjuice.pokego.web.search.form.res.others;

import java.util.List;

import jp.brainjuice.pokego.web.Response;
import jp.brainjuice.pokego.web.search.form.res.elem.SimpPokemon;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class UnimplPokemonResponse extends Response {

	private List<SimpPokemon> unimplList;
	private String lastUpdated;
}
