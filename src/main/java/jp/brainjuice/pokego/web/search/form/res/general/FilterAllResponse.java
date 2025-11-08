package jp.brainjuice.pokego.web.search.form.res.general;

import jp.brainjuice.pokego.business.service.search.pokeFilter.PokemonFilterResult;
import jp.brainjuice.pokego.web.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FilterAllResponse extends Response {

	PokemonFilterResult pfr;

}
