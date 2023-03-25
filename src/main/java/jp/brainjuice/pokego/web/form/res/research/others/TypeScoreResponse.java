package jp.brainjuice.pokego.web.form.res.research.others;

import java.util.List;
import java.util.Map;

import jp.brainjuice.pokego.business.constant.Type.TypeEffectiveEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TypeScoreResponse extends ResearchResponse {

	private boolean isExecutedType;
	private TypeEnum type1;
	private TypeEnum type2;
	private double attacker1Score;
	private double attacker2Score;
	private double defenderScore;
	private Map<TypeEffectiveEnum, List<TypeEnum>> attackerType1Map;
	private Map<TypeEffectiveEnum, List<TypeEnum>> attackerType2Map;
	private Map<TypeEffectiveEnum, List<TypeEnum>> defenderTypeMap;
}
