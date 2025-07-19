package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

/**
 * マスタデータの要素<br>
 * 必要な項目、有用そうな項目のみ定義している。
 */
@Data
@ToString
public class PokemonSettings implements Serializable, Cloneable {

	private String pokemonId;
	private String type;
	private String type2;
	private Map<String, Object> encounter;
	private PokemonStats stats;
	private List<String> quickMoves;
	private List<String> cinematicMoves;
	private float pokedexHeightM;
	private float pokedexWeightKg;
	private int candyToEvolve;
	private float kmBuddyDistance;
	private String form;
	private Shadow shadow;
	private List<String> eliteQuickMove;
	private List<String> eliteCinematicMove;
	private List<FormChange> formChange;
	private List<String> nonTmCinematicMoves;
}
