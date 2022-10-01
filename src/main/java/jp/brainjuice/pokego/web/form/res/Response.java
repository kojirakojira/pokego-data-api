package jp.brainjuice.pokego.web.form.res;

import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import lombok.Data;

@Data
public abstract class Response {

	private boolean success;
	private String message;
	private PokemonSearchResult pokemonSearchResult;
	private String pokedexId;
	private String name;
}
