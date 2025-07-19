package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto;

import java.util.List;
import java.util.Map;

import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.PokemonData;
import lombok.Data;

@Data
public class ParsedMasterData {

	private List<QuickMoveAll> quickMoveList;
	private List<CinematicMoveAll> cinematicMoveList;
	private List<PokemonData> pokemonDataList;
	private Map<String, List<AdditionalMove>> additionalCinematicMoveMap;
}
