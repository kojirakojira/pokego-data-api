package jp.brainjuice.pokego.business.service.utils.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * ポケモン統計情報を保持するクラスです。
 *
 * @author saibabanagchampa
 *
 */
@Component
@Getter
@Setter(lombok.AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor
@Slf4j
public class PokemonStatisticsInfo implements Cloneable {

	private PokedexStats pokedexStats;
	private GoPokedexStats goPokedexStats;

	/**
	 * DIで生成する用のコンストラクタ
	 *
	 * @param pokedexRepository
	 * @param goPokedexRepository
	 */
	@Autowired
	public PokemonStatisticsInfo(
			PokedexRepository pokedexRepository, GoPokedexRepository goPokedexRepository) {

		List<Pokedex> pokedexList = pokedexRepository.findAll();
		List<GoPokedex> goPokedexList = goPokedexRepository.findAll();
		// 全ポケモンを対象とした統計情報をDIに設定する。
		create(pokedexList, goPokedexList);

		log.info("PokemonStatisticsInfo generated!! (Referenced file: none.)");
	}

	/**
	 * DI以外で生成する用のコンストラクタ
	 *
	 * @param iterable
	 * @param iterable2
	 */
	public PokemonStatisticsInfo(Iterable<Pokedex> iterable, Iterable<GoPokedex> iterable2) {
		create(iterable, iterable2);
	}

	/**
	 * 引数に指定したPokedexリストを対象に統計情報を生成する。
	 *
	 * @param pokedexList
	 * @return
	 */
	public PokedexStats createPokedexStats(List<Pokedex> pokedexList) {

		return new PokedexStats(pokedexList);
	}

	/**
	 * 引数に指定したGoPokedexリストを対象に統計情報を生成する。
	 *
	 * @param goPokedexList
	 * @return
	 */
	public GoPokedexStats createGoPokedexStats(List<GoPokedex> goPokedexList) {

		return new GoPokedexStats(goPokedexList);
	}

	/**
	 * ポケモン（原作）の統計情報
	 *
	 * @author saibabanagchampa
	 *
	 */
	@Data
	public class PokedexStats implements Cloneable {

		private Statistics hpStats;
		private Statistics atStats;
		private Statistics dfStats;
		private Statistics spAtStats;
		private Statistics spDfStats;
		private Statistics spStats;

		public PokedexStats(Iterable<Pokedex> iterable) {

			List<Integer> hpList = new ArrayList<>();
			List<Integer> atList = new ArrayList<>();
			List<Integer> dfList = new ArrayList<>();
			List<Integer> spAtList = new ArrayList<>();
			List<Integer> spDfList = new ArrayList<>();
			List<Integer> spList = new ArrayList<>();
			iterable.forEach(p -> {
				hpList.add(p.getHp());
				atList.add(p.getAttack());
				dfList.add(p.getDefense());
				spAtList.add(p.getSpecialAttack());
				spDfList.add(p.getSpecialDefense());
				spList.add(p.getSpeed());
			});
			setHpStats(createStatistics(hpList));
			setAtStats(createStatistics(atList));
			setDfStats(createStatistics(dfList));
			setSpAtStats(createStatistics(spAtList));
			setSpDfStats(createStatistics(spDfList));
			setSpStats(createStatistics(spList));
		}

		public PokedexStats clone() {
			PokedexStats pokedexStats = null;
			try {
				pokedexStats = (PokedexStats) super.clone();
			} catch (CloneNotSupportedException e) {
				log.error("Clone failed.", e);
			}
			return pokedexStats;
		}
	}

	/**
	 * ポケモンGOの統計情報
	 *
	 * @author saibabanagchampa
	 *
	 */
	@Data
	public class GoPokedexStats implements Cloneable {

		private Statistics goAtStats;
		private Statistics goDfStats;
		private Statistics goHpStats;

		public GoPokedexStats(Iterable<GoPokedex> iterable2) {

			List<Integer> hpList = new ArrayList<>();
			List<Integer> atList = new ArrayList<>();
			List<Integer> dfList = new ArrayList<>();
			iterable2.forEach(p -> {
				hpList.add(p.getHp());
				atList.add(p.getAttack());
				dfList.add(p.getDefense());
			});
			setGoHpStats(createStatistics(hpList));
			setGoAtStats(createStatistics(atList));
			setGoDfStats(createStatistics(dfList));
		}

		public GoPokedexStats clone() {
			GoPokedexStats goPokedexStats = null;
			try {
				goPokedexStats = (GoPokedexStats) super.clone();
			} catch (CloneNotSupportedException e) {
				log.error("Clone failed.", e);
			}
			return goPokedexStats;
		}
	}

	/**
	 * 統計情報（リスト、最大値、最小値、中央値）
	 *
	 * @author saibabanagchampa
	 *
	 */
	@Data
	public class Statistics implements Cloneable {

		private List<Integer> list;
		private int max;
		private int min;
		private double med;

		public Statistics clone() {
			Statistics statistics = null;
			try {
				statistics = (Statistics) super.clone();
			} catch (CloneNotSupportedException e) {
				log.error("Clone failed.", e);
			}
			return statistics;
		}

	}

	/**
	 * ポケモン統計情報を生成し、フィールドへセットします。
	 *
	 * @param iterable
	 * @param iterable2
	 */
	public void create(Iterable<Pokedex> iterable, Iterable<GoPokedex> iterable2) {

		// 原作
		setPokedexStats(new PokedexStats(iterable));

		// ポケモンGO
		setGoPokedexStats(new GoPokedexStats(iterable2));

		log.debug(this.toString());

	}

	/**
	 * Statisticsを生成します。
	 *
	 * @param valList
	 * @return
	 */
	private Statistics createStatistics(List<Integer> valList) {

		Statistics stats = new Statistics();

		// 昇順ソート
		Collections.sort(valList);

		stats.setList(valList);
		stats.setMax(calcMax(valList));
		stats.setMin(calcMin(valList));
		stats.setMed(calcMed(valList));

		return stats;
	}

	/**
	 * 最大値を求めます。
	 *
	 * @return
	 */
	private int calcMax(List<Integer> valList) {

		int rtnVal = 0;
		for (Integer v: valList) {
			if (rtnVal < v.intValue()) {
				rtnVal = v.intValue();
			}
		}

		return rtnVal;
	}

	/**
	 * 最小値を求めます。
	 *
	 * @return
	 */
	private int calcMin(List<Integer> valList) {

		int rtnVal = 999;
		for (Integer v: valList) {
			if (v.intValue() < rtnVal) {
				rtnVal = v.intValue();
			}
		}

		return rtnVal;
	}

	/**
	 * 中央値を求めます。
	 *
	 * @param valList
	 * @return
	 */
	private double calcMed(List<Integer> valList) {

		// 昇順ソート
		Collections.sort(valList);

		double med = 0;
		if (valList.size() % 2 == 0) {
			// 偶数
			int medIdx1 = (int) Math.ceil(valList.size() / 2);
			int medIdx2 = medIdx1 + 1;
			med = Math.round((valList.get(medIdx1) + valList.get(medIdx2)) / 2.0);

		} else {
			// 奇数
			int medIdx = ((int) Math.ceil(valList.size() / 2));
			med = valList.get(medIdx).intValue();
		}
		return med;
	}

	public PokemonStatisticsInfo clone() {
		PokemonStatisticsInfo pokemonStatisticsInfo = null;
		try {
			pokemonStatisticsInfo = (PokemonStatisticsInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone failed.", e);
		}
		return pokemonStatisticsInfo;
	}

}
