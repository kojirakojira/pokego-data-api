package jp.brainjuice.pokego.business.service.utils;

import java.util.ArrayList;
import java.util.Collections;

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

	@Autowired
	public PokemonGoUtils(CpMultiplierMap cpMultiplierMap) {
		this.cpMultiplierMap = cpMultiplierMap;
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
	public int culcMaxCp(GoPokedex goPokedex) {

		// 個体値
		return culcMaxCp(
				goPokedex.getAttack(),
				goPokedex.getDefense(),
				goPokedex.getHp());
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
	public int culcCp(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp, String pl) {

		return culcCp(
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
	public int culcCp(int bsAttack, int bsDefense, int bsHp, int ivAttack, int ivDefense, int ivHp, String pl) {

		return culcCp(
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
	public int culcBaseCp(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp) {

		return culcBaseCp(
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
	public int culcBaseCp(int bsAttack, int bsDefense, int bsHp, int ivAttack, int ivDefense, int ivHp) {

		return culcBaseCp(
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
	public int culcMaxIvCp(int attack, int defense, int hp, String pl) {

		// DIのCpMultiplierMapを使用する。
		return culcMaxIvCp(attack, defense, hp, pl, cpMultiplierMap);
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
	public int culcMaxCp(int attack, int defense, int hp) {

		// DIのCpMultiplierMapを使用する。
		return culcMaxCp(attack, defense, hp, cpMultiplierMap);
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
	public int culcBaseCp(int attack, int defense, int hp) {

		// DIのCpMultiplierMapを使用する。
		return culcBaseCp(attack, defense, hp, cpMultiplierMap);
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
	public int culcCp(GoPokedex goPokedex, String pl) {

		// DIのCpMultiplierMapを使用する。
		return culcCp(
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
	public int culcCp(int attack, int defense, int hp, String pl) {

		// DIのCpMultiplierMapを使用する。
		return culcCp(attack, defense, hp, pl, cpMultiplierMap);
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
	private int culcMaxCp(int attack, int defense, int hp, CpMultiplierMap cpMultiplierMap) {

		return culcMaxIvCp(attack,
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
	private int culcMaxIvCp(int attack, int defense, int hp, String pl, CpMultiplierMap cpMultiplierMap) {

		return culcCp(attack + 15,
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
	private int culcBaseCp(int attack, int defense, int hp, CpMultiplierMap cpMultiplierMap) {

		return culcCp(attack, defense, hp, BASE_PL, cpMultiplierMap);
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
	private int culcCp(int attack, int defense, int hp, String pl, CpMultiplierMap cpMultiplierMap) {

		int cp = (int) Math.floor(culcPlainCp(attack, defense, hp) * (Math.pow(cpMultiplierMap.get(pl), 2.0)) / 10.0);

		if (cp <= 10) {
			cp = 10;
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
	public double culcPlainCp(int attack, int defense, int hp) {

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
	public double culcScp(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp, String pl) {

		return culcScp(
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
	public double culcScp(GoPokedex goPokedex, int ivAttack, int ivDefense, int ivHp, double cpm) {

		return culcScp(
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
	public double culcScp(int attack, int defense, int hp, String pl) {

		// DIのCpMultiplierMapを使用する。
		return culcScp(attack, defense, hp, pl, cpMultiplierMap);
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
	private double culcScp(int attack, int defense, int hp, String pl, CpMultiplierMap cpMultiplierMap) {

		return culcScp(attack, defense, hp, cpMultiplierMap.get(pl));
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
	private double culcScp(int attack, int defense, int hp, double cpm) {

		double scp = culcScp(statusProduct(attack, defense, hp, cpm));

		return scp;
	}

	/**
	 * ステ積からSCPを求めます。
	 *
	 * @param sp
	 * @return
	 */
	public double culcScp(double sp) {

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
					cpRankList.add(new CpRank(iva, ivd, ivh));
				}
			}
		}

		cpRankList.forEach(cr -> {
			// CPを求め、セットする。
			int cp = culcCp(goPokedex, cr.getIva(), cr.getIvd(), cr.getIvh(), pl);
			cr.setCp(cp);

			// 個体値のパーセント
			double percent = (((double) (cr.getIva() + cr.getIvd() + cr.getIvh())) / 45.0) * 100.0;
			percent = (Math.round(percent * 10.0)) / 10.0; // 小数第二位で四捨五入
			cr.setPercent(percent);
		});

		// CPでの降順
		Collections.sort(cpRankList, (o1, o2) -> {
			return o1.getPercent() < o2.getPercent() ? 1 : -1;
		});

		// 順位付け
		for(int i = 0; i < cpRankList.size(); i++) {
			cpRankList.get(i).setRank(i + 1);
		}

		return cpRankList;

	}

	public void appendRemarks(Iterable<GoPokedex> goPokedexes) {
		goPokedexes.forEach(gp -> {
			appendRemarks(gp);
		});
	}

	/**
	 * 名前に備考を連結させます。remarksが空文字の場合は何もしません。<br>
	 * format: name + "(" + remarks + ")"
	 *
	 * @param goPokedex
	 */
	public void appendRemarks(GoPokedex goPokedex) {
		String remarks = goPokedex.getRemarks().isEmpty() ? "": "(" + goPokedex.getRemarks() + ")";
		goPokedex.setName(goPokedex.getName() + remarks);
	}
}
