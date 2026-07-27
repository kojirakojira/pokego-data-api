package jp.brainjuice.pokego.business.service.search.utils.dto.pinnacle;

import jp.brainjuice.pokego.business.constant.LearningPatternEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import lombok.Data;

@Data
public class PokemonAttackCombination implements Cloneable {

	private GoPokedex goPokedex;

	private String faMoveId;
	private String faName;
	private TypeEnum faType;
	private LearningPatternEnum fastAttackLearningPattern;

	private String caMoveId;
	private String caName;
	private TypeEnum caType;
	private LearningPatternEnum chargedAttackLearningPattern;

	private double faAttackScore;
	private double caAttackScore;
	private double attackScore;

	private boolean shadow;
	private boolean mega;
	private String attribute; // シャドウ or メガが入る
}
