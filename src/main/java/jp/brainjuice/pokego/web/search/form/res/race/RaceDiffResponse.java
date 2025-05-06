package jp.brainjuice.pokego.web.search.form.res.race;

import java.util.List;

import jp.brainjuice.pokego.business.service.search.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.web.search.form.res.Response;
import jp.brainjuice.pokego.web.search.form.res.elem.Race;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RaceDiffResponse extends Response {

	// nameArrから検索した場合はnull
	private MultiSearchResult msr;
	// allUniqueがfalseの場合はnull
	private List<Race> raceArr;

	private boolean searchedById;

	/** GOにおけるポケモン数（絞り込みをした場合はその数） */
	private int goTotalCount;
	/**
	 * 原作におけるポケモン数（絞り込みをした場合はその数。
	 * アーマードミュウツーのようなポケモンを含む場合、GOと総数が異なる。）
	 */
	private int oriTotalCount;
}
