package jp.brainjuice.pokego.web.form.res.type;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import jp.brainjuice.pokego.business.constant.Type.TypeEffectiveEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TypeScoreResponse extends ResearchResponse {

	private boolean executedType;
	private TypeEnum type1;
	private TypeEnum type2;
	private double attacker1Score;
	private double attacker2Score;
	private double defenderScore;
	private Map<TypeEffectiveEnum, List<TypeEnum>> attackerType1Map;
	private Map<TypeEffectiveEnum, List<TypeEnum>> attackerType2Map;
	private Map<TypeEffectiveEnum, List<TypeEnum>> defenderTypeMap;
	private LinkedHashSet<String> typeComments;
}
