package jp.brainjuice.pokego.business.service.search.utils.dto.pinnacle;

import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonFastAttack;
import lombok.Data;

@Data
public class GymRaidPinnacleRankTempDto {

	private GoPokedex goPokedex;

	private PokemonFastAttack pfa;
	private PokemonChargedAttack pca;

	private double fastAttackScore;
	private double chargedAttackScore;
	private double attackScore;

	private boolean shadow;
}
