package jp.brainjuice.pokego.web.search.form.res.others;

import java.util.LinkedHashMap;
import java.util.List;

import jp.brainjuice.pokego.business.service.search.others.EvoCostResearchService.Costs;
import jp.brainjuice.pokego.web.search.form.res.Response;
import jp.brainjuice.pokego.web.search.form.res.elem.EvolutionEdge;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class EvoCostResponse extends Response {

	private Costs costs;
	private LinkedHashMap<String, LinkedHashMap<String, List<EvolutionEdge>>> costTypeMap;

}
