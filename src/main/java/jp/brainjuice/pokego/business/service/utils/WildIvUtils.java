package jp.brainjuice.pokego.business.service.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.memory.CpMultiplierMap;
import jp.brainjuice.pokego.web.form.res.elem.VersatilityIv;

@Component
public class WildIvUtils {

	private PokemonGoUtils pokemonGoUtils;

	private CpMultiplierMap cpMultiplierMap;

	private static final String WILD_MIN_PL = "1";

	private static final String WILD_MIN_PL_WB = "6";

	private static final String WILD_MAX_PL = "30";

	private static final String WILD_MAX_PL_WB = "35";

	private static final int MIN_IV = 0;

	private static final int MAX_IV = 15;

	private static final int MIN_IV_WB = 4;

	@Autowired
	public WildIvUtils(
			PokemonGoUtils pokemonGoUtils,
			CpMultiplierMap cpMultiplierMap) {
		this.pokemonGoUtils = pokemonGoUtils;
		this.cpMultiplierMap = cpMultiplierMap;
	}

	/**
	 * そのポケモンのそのCP（天候ブースト考慮）において有り得るPLのリストを取得します。
	 * 返却されるリストは、PLが低い順（≒個体値が高い順）に並んでいます。
	 *
	 * @param gp
	 * @param cp
	 * @param wbFlg
	 * @return List<Map.Entry<String, Double>> List<Entry<PL(数値を文字列で保持), PLに対応するCpMultiplier>>
	 */
	public List<Map.Entry<String, Double>> subListByRange(GoPokedex gp, int cp, boolean wbFlg) {

		// 最低個体値、最高個体値において、CPが有り得る境界のindexを算出する。
		int minIvIdx = calcMinIvIdx(gp, cp, wbFlg);
		int maxIvIdx = calcMaxIvIdx(gp, cp, wbFlg);

		// 小さい方をstart, 大きい方をendとする。
		int start = minIvIdx < maxIvIdx ? minIvIdx : maxIvIdx;
		int end = minIvIdx < maxIvIdx ? maxIvIdx : minIvIdx;

		// 天候ブーストを考慮し、最低PL（PL1 or 6）、最高PL（PL30 or 35）を取得する。
		int minIdx = cpMultiplierMap.indexOf(wbFlg ? WILD_MIN_PL_WB : WILD_MIN_PL);
		int maxIdx = cpMultiplierMap.indexOf(wbFlg ? WILD_MAX_PL_WB : WILD_MAX_PL);

		if (!(minIdx <= start && end <= maxIdx)) {
			// 完全に範囲外の場合
			return null;
		}

		// start、endの片方のみが範囲外の場合はPLの枠内に補正する。
		start = minIdx <= start ? start : minIdx;
		end = end <= maxIdx ? end : maxIdx;

		// cpMultiplierListの取得
		List<Map.Entry<String, Double>> cpMultList = cpMultiplierMap.getList();

		return cpMultList.subList(start, end);

	}

	/**
	 * そのポケモンにおけるそのCPにおいて、ありうるPLの範囲における最大のPLのインデックスを求めます。
	 *
	 * @param gp
	 * @param cp
	 * @param wbFlg
	 * @param cpMultList
	 * @return
	 */
	private int calcMinIvIdx(GoPokedex gp, int cp, boolean wbFlg) {

		// 天候ブーストを考慮し、最低個体値を取得する。
		int minIv = wbFlg ? MIN_IV_WB : MIN_IV;

		// 最低個体値の場合にcalcedCp <= cpとなるPLの最大PLのインデックスを取得する。
		// （つまりは、そのポケモンそのCPにおいて、ありうる最大のPLを特定している。）
		int idx = pokemonGoUtils.binarySearchForPlIdx(
				(calcedCp) -> calcedCp <= cp,
				(mult) -> pokemonGoUtils.calcCp(gp, minIv, minIv, minIv, mult));

		return idx;

	}

	/**
	 * そのポケモンにおけるそのCPにおいて、ありうるPLの範囲における最小のPLのインデックスを求めます。
	 *
	 * @param gp
	 * @param cp
	 * @param wbFlg
	 * @return
	 */
	private int calcMaxIvIdx(GoPokedex gp, int cp, boolean wbFlg) {

		// 最高個体値の場合にcalcedCp < cpとなるPLの最低PLのインデックスを取得する。
		// （つまりは、そのポケモンそのCPにおいて、ありうる最低のPLを特定している。）
		int idx = pokemonGoUtils.binarySearchForPlIdx(
				(calcedCp) -> calcedCp < cp,
				(mult) -> pokemonGoUtils.calcCp(gp, MAX_IV, MAX_IV, MAX_IV, mult));

		return idx;
	}

	/**
	 * @param gp
	 * @param cp
	 * @param rangeList
	 * @param wbFlg
	 * @return
	 */
	public List<VersatilityIv> getIvList(GoPokedex gp, int cp, List<Map.Entry<String, Double>> rangeList, boolean wbFlg) {

		List<VersatilityIv> ivList = new ArrayList<>();
		// PLでループさせる
		for (Map.Entry<String, Double> plEntry: rangeList) {
			List<VersatilityIv> ivListOfPl = getIvListOfPl(gp, cp, plEntry, wbFlg);
			if (ivListOfPl != null) {
				ivList.addAll(ivListOfPl);
			}
		}

		int no = 1;
		for (VersatilityIv iv: ivList) {
			iv.setNo(no++);
		}
		return ivList;
	}

	/**
	 * 該当のCPかつ該当のPLの個体値のリストを取得する。
	 *
	 * @param gp
	 * @param cp
	 * @param plEntry
	 * @param wbFlg
	 * @return
	 */
	private List<VersatilityIv> getIvListOfPl(GoPokedex gp, int cp, Map.Entry<String, Double> plEntry, boolean wbFlg) {

		List<VersatilityIv> ivListOfPl = null;
		int minIv = wbFlg ? MIN_IV_WB : MIN_IV;
		int maxIv = MAX_IV;
		// 攻撃、防御、HPのループ
		for(int iva = minIv; iva <= maxIv; iva++) {
			for (int ivd = minIv; ivd <= maxIv; ivd++) {
				for (int ivh = minIv; ivh <= maxIv; ivh++) {

					int calculatedCp = pokemonGoUtils.calcCp(gp, iva, ivd, ivh, plEntry.getValue());
					if (calculatedCp == cp) {
						if (ivListOfPl == null) {
							ivListOfPl = new ArrayList<>();
						}

						ivListOfPl.add(new VersatilityIv(0, plEntry.getKey(), iva, ivd, ivh, pokemonGoUtils.calcPercentIv(iva, ivd, ivh)));
					}

					if (cp < calculatedCp) {
						// 算出したCPが該当のCPを超えていたら、HPのループを中断する。
						// 攻撃、防御の中断処理も入れたほうが合理的になるように思えるが、
						// 前段階でPLの範囲を絞り込んでいるため、無駄な処理はそんなに多くない。
						break;
					}
				}
			}
		}

		return ivListOfPl;
	}
}
