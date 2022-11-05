package jp.brainjuice.pokego.web.form.res;

import java.util.List;

import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RaceDiffResponse extends Response {

	private MultiSearchResult msr;
	// allUniqueがfalseの場合はnull
	private List<Race> raceArr;

	private PokemonStatisticsInfo statistics;
}
