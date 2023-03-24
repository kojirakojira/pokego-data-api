package jp.brainjuice.pokego.web.form.res.research.others;

import java.util.List;
import java.util.Map;

import jp.brainjuice.pokego.business.service.utils.dto.Hierarchy;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class EvolutionResponse extends ResearchResponse {

	// ツリーリスト<yリスト<xリスト<Hierarchy>>>
	private List<List<List<Hierarchy>>> evoTreeInfo;
	private List<String> anotherForms;
	private List<String> bfAfAotForms;
	private Map<String, Race> raceMap;
}
