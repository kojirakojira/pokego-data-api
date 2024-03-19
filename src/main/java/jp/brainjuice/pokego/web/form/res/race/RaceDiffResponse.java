package jp.brainjuice.pokego.web.form.res.race;

import java.util.List;

import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.web.form.res.Response;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RaceDiffResponse extends Response {

	// nameArrから検索した場合はnull
	private MultiSearchResult msr;
	// allUniqueがfalseの場合はnull
	private List<Race> raceArr;

	private PokemonStatisticsInfo statistics;

	private boolean searchedById;
}
