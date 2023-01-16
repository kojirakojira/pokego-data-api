package jp.brainjuice.pokego.web.form.res.research;

import java.util.List;
import java.util.Map;

import jp.brainjuice.pokego.business.service.utils.dto.Hierarchy;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class EvolutionResponse extends ResearchResponse {

	private List<List<Hierarchy>> evoTree;
	private Map<String, Race> raceMap;
}
