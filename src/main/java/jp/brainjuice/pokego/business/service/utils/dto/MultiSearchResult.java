package jp.brainjuice.pokego.business.service.utils.dto;

import java.util.List;

import lombok.Data;

@Data
public class MultiSearchResult {

	private String message;
	private boolean allUnique;
	private List<PokemonSearchResult> psrArr;
}
