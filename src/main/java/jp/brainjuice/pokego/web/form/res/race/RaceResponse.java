package jp.brainjuice.pokego.web.form.res.race;

import java.util.List;

import jp.brainjuice.pokego.cache.inmemory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.form.res.elem.DispFilterParam;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RaceResponse extends ResearchResponse {

	private Race race;
	private PokemonStatisticsInfo statistics;
	private boolean tooStrong;
	private List<DispFilterParam> filteredItems;
	private boolean included;

	/** GOにおけるポケモン数（絞り込みをした場合はその数） */
	private int goTotalCount;
	/**
	 * 原作におけるポケモン数（絞り込みをした場合はその数。
	 * アーマードミュウツーのようなポケモンではGOと数が異なる。）
	 */
	private int oriTotalCount;
}
