package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * そのポケモンが覚えるすべての技を保持する。
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class PokemonMoveAll {

	private String pokedexId;
	private String templateId;
	private List<PokemonMove> quickMoveList = new ArrayList<>();
	private List<PokemonMove> cinematicMoveList = new ArrayList<>();

	public PokemonMoveAll(String pokedexId, String templateId) {
		setPokedexId(pokedexId);
		setTemplateId(templateId);
	}
}
