package jp.brainjuice.pokego.web.form.res;

import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class SearchAllResponse extends Response {

	PokemonSearchResult pokemonSearchResult;

}
