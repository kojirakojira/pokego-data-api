package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PokemonStats implements Serializable, Cloneable {

	private int baseStamina;
	private int baseAttack;
	private int baseDefense;
}
