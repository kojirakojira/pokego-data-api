package jp.brainjuice.pokego.web.form.res.others;

import java.util.LinkedHashMap;
import java.util.List;

import jp.brainjuice.pokego.business.service.others.EvoCostResearchService.Costs;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.form.res.elem.EvolutionEdge;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class EvoCostResponse extends ResearchResponse {

	private Costs costs;
	private boolean isCandy;
	private boolean isPoke;
	private LinkedHashMap<String, LinkedHashMap<String, List<EvolutionEdge>>> costTypeMap;

}
