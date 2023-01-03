package jp.brainjuice.pokego.business.service.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.memory.CpMultiplierMap;
import jp.brainjuice.pokego.web.form.res.elem.ScpRank;
import lombok.Getter;

@Component
public class ScpRankCulculator {

	private CpMultiplierMap cpMultiplierMap;

	private PokemonGoUtils pokemonGoUtils;

	public static final String LEAGUE = "league";

	@Autowired
	public ScpRankCulculator(
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

	/**
	 * リーグに応じたSCPのランク一覧を取得します。<br>
	 * 第2引数に指定できる文字列は、当クラスのLeagueEnumを参照してください。
	 *
	 * @param goPokedex
	 * @param league
	 * @return
	 */
	public ArrayList<ScpRank> getSummary(GoPokedex goPokedex, String league) {

		ArrayList<ScpRank> scpRankList = null;

		LeagueEnum leagueEnum = LeagueEnum.valueOf(league);
		switch (leagueEnum.leagueCode) {
		case 1:
			// スーパーリーグ
			scpRankList = getSuperLeagueSummary(goPokedex);
			break;
		case 2:
			// ハイパーリーグ
			scpRankList = getHyperLeagueSummary(goPokedex);
			break;
		case 3:
			// マスターリーグ
			scpRankList = getMasterLeagueSummary(goPokedex);
			break;
		}

		return scpRankList;
	}

	/**
	 * スーパーリーグのサマリを取得します。
	 *
	 * @param goPokedex
	 * @return
	 */
	public ArrayList<ScpRank> getSuperLeagueSummary(GoPokedex goPokedex) {

		ArrayList<ScpRank> scpRankList = summary(goPokedex, (arg) -> { return arg <= 1500; });

		sort(scpRankList);

		ranking(scpRankList);

		round(scpRankList);

		return scpRankList;
	}

	/**
	 * ハイパーリーグのサマリを取得します。
	 *
	 * @param goPokedex
	 * @return
	 */
	public ArrayList<ScpRank> getHyperLeagueSummary(GoPokedex goPokedex) {

		ArrayList<ScpRank> scpRankList = summary(goPokedex, (arg) -> { return arg <= 2500; });

		sort(scpRankList);

		ranking(scpRankList);

		round(scpRankList);

		return scpRankList;
	}

	/**
	 * マスターリーグのサマリを取得します。
	 *
	 * @param goPokedex
	 * @return
	 */
	public ArrayList<ScpRank> getMasterLeagueSummary(GoPokedex goPokedex) {

		ArrayList<ScpRank> scpRankList = summary(goPokedex, (arg) -> { return true; });

		sort(scpRankList);

		ranking(scpRankList);

		round(scpRankList);

		return scpRankList;
	}

	/**
	 * 個体値ごとの"ScpRank"のリストを作成します。
	 *
	 * @param goPokedex
	 * @param league
	 * @return
	 */
	private ArrayList<ScpRank> summary(GoPokedex goPokedex, Predicate<Integer> cpLimitPredicate) {

		ArrayList<ScpRank> scpRankList = new ArrayList<ScpRank>();
		DecimalFormat plFormat = new DecimalFormat("0.#");
		// 攻撃、防御、HPのループ
		for(int iva = 0; iva <= 15; iva++) {
			for (int ivd = 0; ivd <= 15; ivd++) {
				for (int ivh = 0; ivh <= 15; ivh++) {

					// PLは上からループさせ、CP制限を上回る最大のCPの場合、その個体値のCP,ステ積、SCP、PLをリストに追加する。
					double scp = 0.0; // SCP
					double sp = 0.0; // ステ積
					String pl = null; // PL
					Integer cp = null; // CP
					for (int tpl = 510; tpl >= 10; tpl-=5) {
						pl = plFormat.format(tpl / 10.0);
						int tmpCp = pokemonGoUtils.calcCp(goPokedex, iva, ivd, ivh, pl);

						if (cpLimitPredicate.test(tmpCp)) {
							// CP制限を超えていない場合
							cp = Integer.valueOf(tmpCp);
							sp = pokemonGoUtils.statusProduct(goPokedex, iva, ivd, ivh, cpMultiplierMap.get(pl));
							scp = pokemonGoUtils.calcScp(sp);
							break;
						}

					}

					// （PL1でもCP制限を上回る場合はnullになる。）
					if (cp != null) {
						ScpRank scpRank = new ScpRank(iva, ivd, ivh);
						scpRank.setCp(cp);
						scpRank.setScp(scp);
						scpRank.setSp(sp);
						scpRank.setPl(pl);
						scpRankList.add(scpRank);
					}

				}
			}
		}

		return scpRankList;
	}

	/**
	 * ステ積での降順に並べ替えます。
	 *
	 * @param scpRankList
	 */
	private void sort(ArrayList<ScpRank> scpRankList) {

		// ステ積での降順
		Collections.sort(scpRankList, (o1, o2) -> {
			if (o1.getSp() < o2.getSp()) {
				return 1;
			} else if (o1.getSp() == o2.getSp()) {
				// ステ積が一致する場合は、CPでの降順
				if (o1.getCp() < o2.getCp()) {
					return 1;
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		});
	}

	/**
	 * ランキングを求めます。<br>
	 * ※引数に渡すリストの並び順は、ランキングの高い順であることが前提です。<br>
	 * ※ついでにステ積からパーセントを求めちゃいます。
	 *
	 * @param scpRankList
	 */
	private void ranking(ArrayList<ScpRank> scpRankList) {

		double maxSp = scpRankList.get(0).getSp();

		double beforePer = 0.0;
		int beforeRank = 0;
		for (int i = 0; i <scpRankList.size(); i++) {
			ScpRank sr = scpRankList.get(i);
			// (ステ積 / 100%個体のステ積 * 100)の小数点第2位で四捨五入
			sr.setPercent(Math.round((sr.getSp() / maxSp * 100.0) * 100.0) / 100.0);

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
	 * SCPとステ積を小数点第二位で四捨五入します。
	 *
	 * @param scpRankList
	 */
	private void round(ArrayList<ScpRank> scpRankList) {

		scpRankList.forEach(sr -> {
			sr.setScp(Math.round(sr.getScp() * 100.0) / 100.0);
			sr.setSp(Math.round(sr.getSp() * 100.0) / 100.0);
		});
	}
}
