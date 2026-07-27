package jp.brainjuice.pokego.web.search.form.res.moves;

import java.util.List;

import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispPokemonChargedAttack;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispPokemonFastAttack;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PokemonAttackResponse extends ResearchResponse {

	private List<DispPokemonFastAttack> fastAttackList;
	private List<DispPokemonChargedAttack> chargedAttackList;
	private GoPokedex goPokedex;
	private GoPokedex preMegaGp;

}
