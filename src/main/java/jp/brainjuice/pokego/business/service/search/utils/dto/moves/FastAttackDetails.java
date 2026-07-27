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
public class FastAttackDetails {

	private String generalDescription;
	private DispFastAttack fastAttack;
	private FastAttackRank fastAttackRank;
	private List<GoPokedexAndMoveInfo> learnPokemonList;
	private List<DispFastAttack> sameTypeMoveList;
}
