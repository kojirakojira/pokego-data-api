package jp.brainjuice.pokego.web.form.res.research;

import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RaceResponse extends ResearchResponse {

	private Race race;
	private PokemonStatisticsInfo statistics;
}
