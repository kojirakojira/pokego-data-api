package jp.brainjuice.pokego.web.search.form.res.pinnacle;

import java.util.LinkedHashSet;
import java.util.List;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.search.utils.dto.pinnacle.PokemonAttackCombination;
import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class GymRaidPinnacleRankResponse extends ResearchResponse {

	private List<PokemonAttackCombination> combiList;
	private LinkedHashSet<String> typeComments;
	private List<TypeEnum> wbTypeList;

}
