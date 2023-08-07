package jp.brainjuice.pokego.business.service.utils;

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
import jp.brainjuice.pokego.web.form.res.elem.CpRank;

@Component
public class PokemonGoUtils {

	private CpMultiplierMap cpMultiplierMap;

	/** 基礎CP表現時のPL(世間的な"基礎CP"はPL40のCPを指します。) */
	private static final String BASE_PL = "40";

	/** 最大CP表現時のPL(世間的な"最大CP"はPL50のCPを指します。) */
	private static final String MAX_PL ="50";

	/** 最低CP */
	private static final int LOWEST_CP = 10;

	/** calcPlメソッドにおいて、CPに対してPLが複数存在する場合のメッセージ */
	public static final String DUPLICATE = "DUPLICATE";

	/** calcPlメソッドにおいて、CPに対応するPLが存在しない場合のメッセージ */
	public static final String NOT_EXIST = "NOT_EXIST";

	@Autowired
	public PokemonGoUtils(CpMultiplierMap cpMultiplierMap) {
		this.cpMultiplierMap = cpMultiplierMap;
	}

	/**
	 * PLを算出する。<br>
	 * 指定されたCPに対応するPLが存在しない場合は、"NOT_EXIST"を返す。<br>
	 * 指定されたCPに対してPLが複数存在する場合は、"DUPLICATE"を返す。
	 *
	 * @param goPokedex
	 * @param ivAttack
	 * @param ivDefense
	 * @param ivHp
	 * @param cp
	 * @return
	 */
	public String calcPl(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp, int cp) {

		String pl = null;
		// CP Multiplierでループ
		for (Map.Entry<String, Double> entry: cpMultiplierMap.entrySet()) {
			int calcedCp = calcCp(goPokedex, ivAttack, ivDefense, ivHp, entry.getValue().doubleValue());

			// 算出したCPと、引数のCPが一致しない場合はスキップ
			if (calcedCp != cp) continue;

			// CPに対してPLが複数存在する場合の考慮（引数に最低CP(CP:10)が指定されたときのみ起こりうる。）
			if (pl != null) {
				// 前のループで既にplがセットされていた場合
				pl = DUPLICATE;
				break;
			}

			// PLをセット
			pl = entry.getKey();

			// CPが最低CPで無ければ、有無を言わずにPL確定。
			if (cp != LOWEST_CP) break;

		}
		// PLがnullの場合は"NOT_EXIST"を返却する。
		return pl == null ? NOT_EXIST : pl;
	}

	/**
	 * 個体値から最大cpを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @return
	 */
	public int calcMaxCp(GoPokedex goPokedex) {

		// 個体値
		return calcMaxCp(
				goPokedex.getAttack(),
				goPokedex.getDefense(),
				goPokedex.getHp());
	}

	/**
	 * GoPokedex、個体値、CP Multiplierからcpを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param goPokedex
	 * @param ivAttack
	 * @param ivDefense
	 * @param ivHp
	 * @param multiplier
	 * @return
	 */
	public int calcCp(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp, double multiplier) {

		return calcCp(
				goPokedex.getAttack() + ivAttack,
				goPokedex.getDefense() + ivDefense,
				goPokedex.getHp() + ivHp,
				multiplier);
	}

	/**
	 * GoPokedex、個体値からcpを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param goPokedex
	 * @param ivAttack
	 * @param ivDefense
	 * @param ivHp
	 * @param pl
	 * @return
	 */
	public int calcCp(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp, String pl) {

		return calcCp(
				goPokedex.getAttack() + ivAttack,
				goPokedex.getDefense() + ivDefense,
				goPokedex.getHp() + ivHp,
				pl);
	}

	/**
	 * 種族値・個体値からcpを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param bsAttack
	 * @param bsDefense
	 * @param bsHp
	 * @param ivAttack
	 * @param ivDefense
	 * @param ivHp
	 * @param pl
	 * @return
	 */
	public int calcCp(int bsAttack, int bsDefense, int bsHp, int ivAttack, int ivDefense, int ivHp, String pl) {

		return calcCp(
				bsAttack + ivAttack,
				bsDefense + ivDefense,
				bsHp + ivHp,
				pl);
	}

	/**
	 * GoPokedex、個体値からcpを求めます。<br>
	 * PL40の場合のCPを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param goPokedex
	 * @param ivAttack
	 * @param ivDefense
	 * @param ivHp
	 * @return
	 */
	public int calcBaseCp(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp) {

		return calcBaseCp(
				goPokedex.getAttack() + ivAttack,
				goPokedex.getDefense() + ivDefense,
				goPokedex.getHp() + ivHp);
	}

	/**
	 * 種族値・個体値からcpを求めます。<br>
	 * PL40の場合のcpを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param bsAttack
	 * @param bsDefense
	 * @param bsHp
	 * @param ivAttack
	 * @param ivDefense
	 * @param ivHp
	 * @return
	 */
	public int calcBaseCp(int bsAttack, int bsDefense, int bsHp, int ivAttack, int ivDefense, int ivHp) {

		return calcBaseCp(
				bsAttack + ivAttack,
				bsDefense + ivDefense,
				bsHp + ivHp);
	}

	/**
	 * 個体値100%の最大cpを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @return
	 */
	public int calcMaxIvCp(int attack, int defense, int hp, String pl) {

		// DIのCpMultiplierMapを使用する。
		return calcMaxIvCp(attack, defense, hp, pl, cpMultiplierMap);
	}

	/**
	 * 個体値から最大cpを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @return
	 */
	public int calcMaxCp(int attack, int defense, int hp) {

		// DIのCpMultiplierMapを使用する。
		return calcMaxCp(attack, defense, hp, cpMultiplierMap);
	}

	/**
	 * PL40の場合のCPを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param cpMultiplierMap
	 * @return
	 */
	public int calcBaseCp(int attack, int defense, int hp) {

		// DIのCpMultiplierMapを使用する。
		return calcBaseCp(attack, defense, hp, cpMultiplierMap);
	}

	/**
	 * PLに応じたCPを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param pl
	 * @return
	 */
	public int calcCp(GoPokedex goPokedex, String pl) {

		// DIのCpMultiplierMapを使用する。
		return calcCp(
				goPokedex.getAttack(),
				goPokedex.getDefense(),
				goPokedex.getHp(),
				pl,
				cpMultiplierMap);
	}

	/**
	 * PLに応じたCPを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param pl
	 * @return
	 */
	public int calcCp(int attack, int defense, int hp, String pl) {

		// DIのCpMultiplierMapを使用する。
		return calcCp(attack, defense, hp, pl, cpMultiplierMap);
	}

	/**
	 * 最大CPを求めます。(世間的な"最大CP"はCP50を指します。)<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param cpMultiplierMap
	 * @return
	 */
	private int calcMaxCp(int attack, int defense, int hp, CpMultiplierMap cpMultiplierMap) {

		return calcMaxIvCp(attack,
				defense,
				hp,
				MAX_PL,
				cpMultiplierMap);
	}

	/**
	 * 個体値100%のCPを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param cpMultiplierMap
	 * @return
	 */
	private int calcMaxIvCp(int attack, int defense, int hp, String pl, CpMultiplierMap cpMultiplierMap) {

		return calcCp(attack + 15,
				defense + 15,
				hp + 15,
				pl,
				cpMultiplierMap);
	}

	/**
	 * PL40の場合のCPを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param cpMultiplierMap
	 * @return
	 */
	private int calcBaseCp(int attack, int defense, int hp, CpMultiplierMap cpMultiplierMap) {

		return calcCp(attack, defense, hp, BASE_PL, cpMultiplierMap);
	}

	/**
	 * CPを求めます。<br>
	 * 返却値 = (攻撃 * √防御 * √HP) * CPM ^ 2 / 10 (最小値：10）
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param pl
	 * @param cpMultiplierMap
	 * @return
	 */
	private int calcCp(int attack, int defense, int hp, String pl, CpMultiplierMap cpMultiplierMap) {

		int cp = (int) Math.floor(calcPlainCp(attack, defense, hp) * (Math.pow(cpMultiplierMap.get(pl).doubleValue(), 2.0)) / 10.0);

		if (cp <= LOWEST_CP) {
			cp = LOWEST_CP;
		}
		return cp;
	}

	/**
	 * CPを求めます。<br>
	 * 返却値 = (攻撃 * √防御 * √HP) * CPM ^ 2 / 10 (最小値：10）
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param multiplier
	 * @return
	 */
	private int calcCp(int attack, int defense, int hp, double multiplier) {

		int cp = (int) Math.floor(calcPlainCp(attack, defense, hp) * (Math.pow(multiplier, 2.0)) / 10.0);

		if (cp <= LOWEST_CP) {
			cp = LOWEST_CP;
		}
		return cp;
	}

	/**
	 * "素のCP"を求めます。<br>
	 * ※CPM等を考慮する前の値です。<br>
	 * 返却値 = 攻撃 * √防御 * √HP
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @return
	 */
	public double calcPlainCp(int attack, int defense, int hp) {

		double attackD = Integer.valueOf(attack).doubleValue();
		double defenseD = Integer.valueOf(defense).doubleValue();
		double hpD = Integer.valueOf(hp).doubleValue();

		double plainCp = attackD * Math.sqrt(defenseD) * Math.sqrt(hpD);

		return plainCp;
	}



	/**
	 * GoPokedex、個体値からSCPを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param goPokedex
	 * @param ivAttack
	 * @param ivDefense
	 * @param ivHp
	 * @param pl
	 * @return
	 */
	public double calcScp(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp, String pl) {

		return calcScp(
				goPokedex.getAttack() + ivAttack,
				goPokedex.getDefense() + ivDefense,
				goPokedex.getHp() + ivHp,
				pl);
	}

	/**
	 * GoPokedex、個体値からSCPを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param goPokedex
	 * @param ivAttack
	 * @param ivDefense
	 * @param ivHp
	 * @param cpm
	 * @return
	 */
	public double calcScp(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp, double cpm) {

		return calcScp(
				goPokedex.getAttack() + ivAttack,
				goPokedex.getDefense() + ivDefense,
				goPokedex.getHp() + ivHp,
				cpm);
	}

	/**
	 * SCPを求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param pl
	 * @return
	 */
	public double calcScp(int attack, int defense, int hp, String pl) {

		// DIのCpMultiplierMapを使用する。
		return calcScp(attack, defense, hp, pl, cpMultiplierMap);
	}

	/**
	 * SCPを求めます。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param pl
	 * @param cpMultiplierMap
	 * @return
	 */
	private double calcScp(int attack, int defense, int hp, String pl, CpMultiplierMap cpMultiplierMap) {

		return calcScp(attack, defense, hp, cpMultiplierMap.get(pl));
	}

	/**
	 * SCPを求めます。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param pl
	 * @param cpMultiplierMap
	 * @return
	 */
	private double calcScp(int attack, int defense, int hp, double cpm) {

		double scp = calcScp(statusProduct(attack, defense, hp, cpm));

		return scp;
	}

	/**
	 * ステ積からSCPを求めます。
	 *
	 * @param sp
	 * @return
	 */
	public double calcScp(double sp) {

		// ((attack * defense * hp) ^ 2 / 3) / 10
		double scp = (Math.pow(sp, 2.0 / 3.0) / 10.0);

		return scp;
	}

	/**
	 * GoPokedex、個体値からステ積を求めます。<br>
	 * ※引数にはGOのステータスを指定してください。
	 *
	 * @param goPokedex
	 * @param ivAttack
	 * @param ivDefense
	 * @param ivHp
	 * @param cpm
	 * @return
	 */
	public double statusProduct(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp, double cpm) {

		return statusProduct(
				goPokedex.getAttack() + ivAttack,
				goPokedex.getDefense() + ivDefense,
				goPokedex.getHp() + ivHp,
				cpm);
	}

	/**
	 * ステ積を求めます。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param cpm
	 * @return
	 */
	public double statusProduct(int attack, int defense, int hp, double cpm) {

		double attackD = Integer.valueOf(attack).doubleValue() * cpm;
		double defenseD = Integer.valueOf(defense).doubleValue() * cpm;
		double hpD = Math.floor(Integer.valueOf(hp).doubleValue() * cpm);

		return attackD * defenseD * hpD;
	}

	/**
	 * 指定した個体値におけるCPランキングの順位を取得します。
	 *
	 * @param goPokedex
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @param pl
	 * @return
	 */
	public CpRank getBaseCpRank(GoPokedex goPokedex, int iva, int ivd, int ivh) {

		return getCpRank(goPokedex, iva, ivd, ivh, BASE_PL);
	}

	/**
	 * 指定した個体値におけるCPランキングの順位を取得します。<br>
	 * PLを考慮します。
	 *
	 * @param goPokedex
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @param pl
	 * @return
	 */
	public CpRank getCpRank(GoPokedex goPokedex, int iva, int ivd, int ivh, String pl) {

		ArrayList<CpRank> cpRankList = getCpRankList(goPokedex, pl);

		CpRank cpRank = cpRankList.stream().filter(
				cr -> cr.getIva() == iva && cr.getIvd() == ivd && cr.getIvh() == ivh
				).findAny().get();

		return cpRank;
	}

	/**
	 * CPランキングの一覧を取得します。
	 *
	 * @param goPokedex
	 * @return
	 */
	public ArrayList<CpRank> getBaseCpRankList(GoPokedex goPokedex) {

		return getCpRankList(goPokedex, BASE_PL);
	}

	/**
	 * CPランキングの一覧を取得します。
	 *
	 * @param goPokedex
	 * @param pl
	 * @return
	 */
	public ArrayList<CpRank> getCpRankList(GoPokedex goPokedex, String pl) {

		ArrayList<CpRank> cpRankList = new ArrayList<CpRank>();

		// 各ステータスを0～15ですべてセットする。
		for (int iva = 0; iva <= 15; iva++) {
			for (int ivd = 0; ivd <= 15; ivd++) {
				for (int ivh = 0; ivh <= 15; ivh++) {
					CpRank cpRank = new CpRank(iva, ivd, ivh);

					// CP
					cpRank.setCp(calcCp(goPokedex, iva, ivd, ivh, pl));

					// 個体値のパーセント
					double percent = calcPercentIv(iva, ivd, ivh);
					cpRank.setPercent(percent);

					cpRankList.add(cpRank);
				}
			}
		}

		// CPでの降順
		Collections.sort(cpRankList, (o1, o2) -> {
			return o2.getCp() - o1.getCp();
		});

		// CPのランキングをつくる。
		List<Integer> cpList = cpRankList.stream()
				.map(CpRank::getCp)
				.collect(Collectors.toList());
		// 順位付け
		cpRankList.stream().forEach(cr -> {
			int index = cpList.indexOf(cr.getCp());
			cr.setRank(index + 1);
		});

		return cpRankList;

	}

	/**
	 * 個体値のパーセントを取得します。
	 * (0 - 0 - 0)を0%個体、(15 - 15 - 15)を100%個体と判定します。
	 *
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @return
	 */
	public double calcPercentIv(int iva, int ivd, int ivh) {

		double percent = (((double) (iva + ivd + ivh)) / 45.0) * 100.0;
		percent = (Math.round(percent * 10.0)) / 10.0; // 小数第二位で四捨五入

		return percent;
	}

	/**
	 * 対象のポケモンの特定の個体値における、PLの境界値を求める。
	 * 二分探索を使用して探索を行う。
	 *
	 * @param cpPredicate CPの境界値の判定式
	 * @param calcCpFunction cpMultiplier(Double)を渡して、cp(int)を取得する関数
	 * @return index
	 */
	public int binarySearchForPlIdx(
			Predicate<Integer> cpPredicate,
			Function<Double, Integer> calcCpFunc) {

		List<Map.Entry<String, Double>> cpMultiplierList = cpMultiplierMap.getList();

		return binarySearchForPlIdx(
				cpPredicate,
				calcCpFunc,
				0,
				cpMultiplierList.size(),
				cpMultiplierList);
	}

	/**
	 * 対象のポケモンの特定の個体値における、PLの境界値を求める。
	 * 二分探索を使用して探索を行う。
	 *
	 * @param cpPredicate CPの境界値の判定式
	 * @param calcCpFunction cpMultiplier(Double)を渡して、cp(int)を取得する関数
	 * @param minIdx cpMultiplierListの探索範囲（最低）
	 * @param maxIdx cpMultiplierListの探索範囲（最高）
	 * @return index
	 */
	public int binarySearchForPlIdx(
			Predicate<Integer> cpPredicate,
			Function<Double, Integer> calcCpFunc,
			int minIdx,
			int maxIdx) {

		List<Map.Entry<String, Double>> cpMultiplierList = cpMultiplierMap.getList();

		return binarySearchForPlIdx(
				cpPredicate,
				calcCpFunc,
				minIdx,
				maxIdx,
				cpMultiplierList);
	}

	/**
	 * 対象のポケモンの特定の個体値における、PLの境界値を求める。
	 * 二分探索を使用して探索を行う。
	 *
	 * @param cpPredicate CPの境界値の判定式
	 * @param calcCpFunction cpMultiplier(Double)を渡して、cp(int)を取得する関数
	 * @param minIdx cpMultiplierListの探索範囲（最低）
	 * @param maxIdx cpMultiplierListの探索範囲（最高）
	 * @param cpMultiplierList CpMultiplierListを指定したい場合（ListはPLの低い順でなければならない。）
	 * @return index
	 */
	public int binarySearchForPlIdx(
			Predicate<Integer> cpPredicate,
			Function<Double, Integer> calcCpFunc,
			int minIdx,
			int maxIdx,
			List<Map.Entry<String, Double>> cpMultiplierList) {

		int left = minIdx;
		int right = maxIdx;
		int mid = 0;
		// 中央 = 左 + (右 - 左) / 2になるまでループ
		while (mid != left + (right - left) / 2) {
			mid = left + (right - left) / 2;
			// 中央のCPを求める。
			int cp = calcCpFunc.apply(cpMultiplierList.get(mid).getValue());

			if (cpPredicate.test(cp)) {
				// 中央のCPが制限を超えていない場合、左を狭める。
				left = mid - 1;
			} else {
				// 中央のCPが制限を超えている場合、右を狭める。
				right = mid + 1;
			}
		}
		return mid;
	}
}
