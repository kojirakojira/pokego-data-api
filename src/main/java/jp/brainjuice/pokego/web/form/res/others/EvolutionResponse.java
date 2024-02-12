package jp.brainjuice.pokego.web.form.res.others;

import java.util.List;
import java.util.Map;

import jp.brainjuice.pokego.business.service.utils.dto.evo.Hierarchy;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class EvolutionResponse extends ResearchResponse {

	/**
	 * ツリーリスト<yリスト<xリスト<Hierarchy>>>
	 * ツリーリストは、ガーメイルのような進化前が複数存在する場合にのみ登場する。
	 */
	private String pid;
	private List<List<List<Hierarchy>>> evoTreeInfo;
	private List<String> anotherForms;
	private List<String> bfAfAotForms;
	private Map<String, Race> raceMap;
	private List<String> evoTreeAnnotations;
}
