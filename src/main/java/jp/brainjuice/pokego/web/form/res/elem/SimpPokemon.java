package jp.brainjuice.pokego.web.form.res.elem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpPokemon {

	private String pokedexId;
	private String name;
	private String image;
	private String gen;
	private String remarks;
}
