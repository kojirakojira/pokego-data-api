package jp.brainjuice.pokego.web.search.form.res.general;

import jp.brainjuice.pokego.business.service.search.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.web.search.form.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class SearchAllResponse extends Response {

	PokemonSearchResult pokemonSearchResult;

}
