package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChargedAttackDetails {

	private String generalDescription;
	private DispChargedAttack chargedAttack;
	private ChargedAttackRank chargedAttackRank;
	private List<GoPokedexAndMoveInfo> learnPokemonList;
	private List<DispChargedAttack> sameTypeMoveList;
}
