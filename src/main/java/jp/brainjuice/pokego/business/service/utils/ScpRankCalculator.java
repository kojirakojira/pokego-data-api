package jp.brainjuice.pokego.business.service.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.memory.CpMultiplierMap;
import jp.brainjuice.pokego.web.form.res.elem.ScpRank;
import lombok.Getter;

@Component
public class ScpRankCalculator {

	private CpMultiplierMap cpMultiplierMap;

	private PokemonGoUtils pokemonGoUtils;

	@Autowired
	public ScpRankCalculator(
			CpMultiplierMap cpMultiplierMap,
			PokemonGoUtils pokemonGoUtils) {
		this.cpMultiplierMap = cpMultiplierMap;
		this.pokemonGoUtils = pokemonGoUtils;
	}

	/**
	 * リーグの名称とコードの列挙型
	 *
	 * @author saibabanagchampa
	 *
	 */
	public enum LeagueEnum {
		sl(1), // SUPER LEAGUE(日本語名称)
		gl(1), // GREAT LEAGUE(英語圏名称)
		hl(2), // HYPER LEAGUE(日本語名称)
		ul(2), // ULTRA LEAGUE(英語圏名称)
		ml(3); // MASTER LEAGUE

		@Getter
		private final int leagueCode;

		private LeagueEnum(int leagueCode) {
			this.leagueCode = leagueCode;
		}

	}

	// スーパーリーグ用CP制限判定用Predicate
	private final Predicate<Integer> slCpLimitPredicate = (arg) -> { return arg.intValue() <= 1500; };
	// ハイパーリーグ用CP制限判定用Predicate
	private final Predicate<Integer> hlCpLimitPredicate = (arg) -> { return arg.intValue() <= 2500; };
	// マスターリーグ用CP制限判定用Predicate
	private final Predicate<Integer> mlCpLimitPredicate = (arg) -> { return true; };


	/**
	 * リーグに応じたSCPのランク一覧を取得します。<br>
	 * 第2引数に指定できる文字列は、当クラスのLeagueEnumを参照してください。
	 *
	 * @param goPokedex
	 * @param league
	 * @return
	 */
	public List<ScpRank> getSummary(GoPokedex goPokedex, String league) {

		LeagueEnum leagueEnum = LeagueEnum.valueOf(league);
		List<ScpRank> scpRankList = switch (leagueEnum.leagueCode) {
		// スーパーリーグ
		case 1 -> scpRankList = getSuperLeagueSummary(goPokedex);
		// ハイパーリーグ
		case 2 -> scpRankList = getHyperLeagueSummary(goPokedex);
		// マスターリーグ
		case 3 -> scpRankList = getMasterLeagueSummary(goPokedex);
		default -> throw new IllegalArgumentException("Unexpected value: " + leagueEnum.leagueCode);
		};

		return scpRankList;
	}

	/**
	 * スーパーリーグの指定の個体値のScpRankを取得します。
	 *
	 * @param goPokedex
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @return
	 */
	public ScpRank getSuperLeagueRank(GoPokedex goPokedex, int iva, int ivd, int ivh) {

		List<ScpRank> scpRankList = summary(goPokedex, slCpLimitPredicate);

		sort(scpRankList);

		ranking(scpRankList);

		ScpRank scpRank = getScpRank(scpRankList, iva, ivd, ivh);
		// リーグをセット
		scpRank.setLeague(LeagueEnum.sl);

		return scpRank;
	}

	/**
	 * ハイパーリーグの指定の個体値のScpRankを取得します。
	 *
	 * @param goPokedex
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @return
	 */
	public ScpRank getHyperLeagueRank(GoPokedex goPokedex, int iva, int ivd, int ivh) {

		List<ScpRank> scpRankList = summary(goPokedex, hlCpLimitPredicate);

		sort(scpRankList);

		ranking(scpRankList);

		ScpRank scpRank = getScpRank(scpRankList, iva, ivd, ivh);
		// リーグをセット
		scpRank.setLeague(LeagueEnum.hl);

		return scpRank;
	}

	/**
	 * マスターリーグの指定の個体値のScpRankを取得します。
	 *
	 * @param goPokedex
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @return
	 */
	public ScpRank getMasterLeagueRank(GoPokedex goPokedex, int iva, int ivd, int ivh) {

		List<ScpRank> scpRankList = summary(goPokedex, mlCpLimitPredicate);

		sort(scpRankList);

		ranking(scpRankList);

		ScpRank scpRank = getScpRank(scpRankList, iva, ivd, ivh);
		// リーグをセット
		scpRank.setLeague(LeagueEnum.ml);

		return scpRank;
	}

	/**
	 * 引数に指定された個体値のScpRankを取得します。
	 *
	 * @param scpRankList
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @return
	 */
	public ScpRank getScpRank(List<ScpRank> scpRankList, int iva, int ivd, int ivh) {

		return scpRankList.stream()
				.filter(sr -> sr.getIva() == iva && sr.getIvd() == ivd && sr.getIvh() == ivh)
				.findFirst().get();

	}

	/**
	 * スーパーリーグのサマリを取得します。
	 *
	 * @param goPokedex
	 * @return
	 */
	public List<ScpRank> getSuperLeagueSummary(GoPokedex goPokedex) {

		List<ScpRank> scpRankList = summary(goPokedex, slCpLimitPredicate);

		scpRankList = getListWithLeague(LeagueEnum.sl, scpRankList);

		sort(scpRankList);

		ranking(scpRankList);

		return scpRankList;
	}

	/**
	 * ハイパーリーグのサマリを取得します。
	 *
	 * @param goPokedex
	 * @return
	 */
	public List<ScpRank> getHyperLeagueSummary(GoPokedex goPokedex) {

		List<ScpRank> scpRankList = summary(goPokedex, hlCpLimitPredicate);

		scpRankList = getListWithLeague(LeagueEnum.hl, scpRankList);

		sort(scpRankList);

		ranking(scpRankList);

		return scpRankList;
	}

	/**
	 * マスターリーグのサマリを取得します。
	 *
	 * @param goPokedex
	 * @return
	 */
	public List<ScpRank> getMasterLeagueSummary(GoPokedex goPokedex) {

		List<ScpRank> scpRankList = summary(goPokedex, mlCpLimitPredicate);

		scpRankList = getListWithLeague(LeagueEnum.ml, scpRankList);

		sort(scpRankList);

		ranking(scpRankList);

		return scpRankList;
	}

	/**
	 * 個体値ごとのScpRankのリストを作成します。
	 *
	 * @param goPokedex
	 * @param league
	 * @return
	 */
	private List<ScpRank> summary(GoPokedex goPokedex, Predicate<Integer> cpLimitPredicate) {

		List<ScpRank> scpRankList = new ArrayList<ScpRank>();
		DecimalFormat plFormat = new DecimalFormat("0.#");
		// 攻撃、防御、HPのループ
		for(int iva = 0; iva <= 15; iva++) {
			for (int ivd = 0; ivd <= 15; ivd++) {
				for (int ivh = 0; ivh <= 15; ivh++) {

					ScpRank scpRank = createScpRank(goPokedex, iva, ivd, ivh, plFormat, cpLimitPredicate);
					if (scpRank == null) {
						// 個体値が高すぎてPL:1で既にあり得ない個体値の場合はHPのループをスキップする。
						break;
					}
					scpRankList.add(scpRank);
				}
			}
		}

		return scpRankList;
	}

	/**
	 * CP制限内の最も高い個体値(ScpRank)を取得する。
	 * CP制限内の個体値が存在しない場合は、nullを返却する。
	 *
	 * @param goPokedex
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @param cpLimitPredicate
	 * @param plFormat
	 * @return
	 */
	private ScpRank createScpRank(
			GoPokedex goPokedex,
			int iva,
			int ivd,
			int ivh,
			DecimalFormat plFormat,
			Predicate<Integer> cpLimitPredicate) {

		Function<Double, Integer> calcCpFunc = (pl) -> pokemonGoUtils.calcCp(goPokedex, iva, ivd, ivh, pl);

		/** CP制限内で、最も高い個体値のPL、CPを求める。 */
		// マスターリーグの場合は、固定でPL51が最高個体になるため、まずPL最大値(PL:51)の個体値を調べる。
		// （スーパー、ハイパーリーグの場合もPL最大個体がCP制限を上回るパターンは多いため、処理性能向上に繋がる。）
		List<Map.Entry<String, Double>> cpMultiplierList = cpMultiplierMap.getList();
		String pl = cpMultiplierMap.maxPl();
		int cp = calcCpFunc.apply(cpMultiplierMap.get(pl));

		if (!cpLimitPredicate.test(cp)) {
			// PLが最大値のとき、CP制限に収まっていない場合。（二分探索）
			int plIdx = pokemonGoUtils.binarySearchForPlIdx(
					cpLimitPredicate,
					calcCpFunc);
			// 確定したPLを取得
			pl = cpMultiplierList.get(plIdx).getKey();
			cp = calcCpFunc.apply(cpMultiplierMap.get(pl));
		}

		ScpRank scpRank = null;
		// （PL1でもCP制限を上回る場合はfalseになる。）
		if (cpLimitPredicate.test(cp)) {
			double sp = pokemonGoUtils.statusProduct(goPokedex, iva, ivd, ivh, cpMultiplierMap.get(pl));
			double scp = pokemonGoUtils.calcScp(sp);

			scpRank = new ScpRank(iva, ivd, ivh);
			scpRank.setCp(cp);
			scpRank.setSp(sp);
			scpRank.setScp(scp);
			scpRank.setPl(pl);
			// rankとpercentはここではセットしない。
		}

		return scpRank;
	}

	/**
	 * ステ積での降順に並べ替えます。
	 *
	 * @param scpRankList
	 */
	private void sort(List<ScpRank> scpRankList) {

		// ステ積での降順
		Collections.sort(scpRankList, (o1, o2) -> {
			if (o1.getSp() < o2.getSp()) return 1;
			if (o1.getSp() > o2.getSp()) return -1;

			// ステ積が一致する場合は、CPでの降順
			if (o1.getCp() < o2.getCp()) return 1;
			if (o1.getCp() > o2.getCp()) return -1;

			return 0;
		});
	}

	/**
	 * ランキングを求めます。<br>
	 * ※引数に渡すリストの並び順は、ランキングの高い順であることが前提です。<br>
	 * ※ついでにステ積からパーセントを求めちゃいます。
	 *
	 * @param scpRankList
	 */
	private void ranking(List<ScpRank> scpRankList) {

		double maxSp = scpRankList.get(0).getSp();

		double beforePer = 0.0;
		int beforeRank = 0;
		final int size = scpRankList.size();
		for (int i = 0; i < size; i++) {
			ScpRank sr = scpRankList.get(i);
			// (ステ積 / 100%個体のステ積 * 100)の小数点第2位で四捨五入
			sr.setPercent(sr.getSp() / maxSp * 100.0);

			// 個体値（パーセント）が同じ場合は順位を同じにする。
			if (beforePer == sr.getPercent()) {
				// 1つ前の個体値とパーセントが一致する場合
				sr.setRank(beforeRank);
			} else {
				// 一致しない場合
				sr.setRank(i + 1);
			}

			beforePer = sr.getPercent();
			beforeRank = sr.getRank();
		}
	}

	/**
	 * ScpRankにleagueをセットして返却する。
	 *
	 * @param league
	 * @param scpRankList
	 * @return
	 */
	private List<ScpRank> getListWithLeague(LeagueEnum league, List<ScpRank> scpRankList) {

		return scpRankList.stream()
				.map(sr -> {
					sr.setLeague(league);
					return sr;
				})
				.collect(Collectors.toList());
	}
}
