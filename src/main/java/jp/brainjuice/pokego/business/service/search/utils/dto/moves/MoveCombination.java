package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.Data;

@Data
public class MoveCombination {

	private int no;

	private String faMoveId;
	private String faName;
	private TypeEnum faType;
	private String caMoveId;
	private String caName;
	private TypeEnum caType;

	private double faAttackScore;
	private double caAttackScore;
	private double attackScore;

	private boolean shadow;
}
