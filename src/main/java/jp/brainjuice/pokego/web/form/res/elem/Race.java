package jp.brainjuice.pokego.web.form.res.elem;

import java.util.List;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.cache.inmemory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.cache.inmemory.PokemonStatisticsInfo.GoPokedexStats;
import jp.brainjuice.pokego.cache.inmemory.PokemonStatisticsInfo.PokedexStats;
import jp.brainjuice.pokego.cache.inmemory.PokemonStatisticsInfo.Statistics;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 種族値を表現するクラス
 *
 * @author saibabanagchampa
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Race {

	private String pokedexId;
	private String name;
	private String remarks;
	private Pokedex pokedex;
	private GoPokedex goPokedex;
	/** nullの場合もある。 */
	private RaceOriRank oriRank;
	/** nullの場合もある。 */
	private RaceGoRank goRank;

	public Race(Pokedex pokedex, GoPokedex goPokedex) {

		setPokedexId(goPokedex.getPokedexId());
		setName(goPokedex.getName());
		setRemarks(goPokedex.getRemarks());

		setPokedex(pokedex);
		setGoPokedex(goPokedex);

	}

	/**
	 * 種族値をセットする。
	 * ※順位もセットする。
	 *
	 * @param pokedex
	 * @param goPokedex
	 * @param statistics
	 */
	public Race(Pokedex pokedex, GoPokedex goPokedex, PokemonStatisticsInfo statistics) {

		this(pokedex, goPokedex);

		{
			GoPokedexStats goStats = statistics.getGoPokedexStats();
			int hpRank = rank(goPokedex.getHp(), goStats.getGoHpStats());
			int atRank = rank(goPokedex.getAttack(), goStats.getGoAtStats());
			int dfRank = rank(goPokedex.getDefense(), goStats.getGoDfStats());
			setGoRank(new RaceGoRank(hpRank, atRank, dfRank));
		}

		{
			PokedexStats oriStats = statistics.getPokedexStats();
			int hpRank = rank(pokedex.getHp(), oriStats.getHpStats());
			int atRank = rank(pokedex.getAttack(), oriStats.getAtStats());
			int dfRank = rank(pokedex.getDefense(), oriStats.getDfStats());
			int spAtRank = rank(pokedex.getSpecialAttack(), oriStats.getSpAtStats());
			int spDfRank = rank(pokedex.getSpecialDefense(), oriStats.getSpDfStats());
			int spRank = rank(pokedex.getSpeed(), oriStats.getSpStats());
			setOriRank(new RaceOriRank(hpRank, atRank, dfRank, spAtRank, spDfRank, spRank));
		}

	}

	private int rank(int num, Statistics stats) {

		List<Integer> list = stats.getList();
		return list.size() - list.lastIndexOf(Integer.valueOf(num));
	}
}
