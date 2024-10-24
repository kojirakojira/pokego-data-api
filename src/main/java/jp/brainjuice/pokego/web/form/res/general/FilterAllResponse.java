package jp.brainjuice.pokego.web.form.res.general;

import jp.brainjuice.pokego.business.service.pokeFilter.PokemonFilterResult;
import jp.brainjuice.pokego.web.form.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FilterAllResponse extends Response {

	PokemonFilterResult pfr;

}
