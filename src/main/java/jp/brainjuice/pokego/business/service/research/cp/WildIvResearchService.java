package jp.brainjuice.pokego.business.service.research.cp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.memory.CpMultiplierMap;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.elem.VersatilityIv;
import jp.brainjuice.pokego.web.form.res.research.cp.WildIvResponse;

/**
 * 野生ポケモンの個体値を算出するサービスクラス
 *
 * @author saibabanagchampa
 *
 */
@Service
public class WildIvResearchService implements ResearchService<WildIvResponse> {

	private PokemonGoUtils pokemonGoUtils;

	private CpMultiplierMap cpMultiplierMap;

	private static final String WILD_MIN_PL = "1";

	private static final String WILD_MIN_PL_WB = "6";

	private static final String WILD_MAX_PL = "30";

	private static final String WILD_MAX_PL_WB = "35";

	private static final int MIN_IV = 0;

	private static final int MAX_IV = 15;

	private static final int MIN_IV_WB = 4;

	private static final String NO_HIT_MSG = "該当する個体値が存在しませんでした。";

	private static final String CP_OUT_OF_SCOPE_MSG = "野生ではありえないCPが指定されました。";

	@Autowired
	public WildIvResearchService(
			PokemonGoUtils pokemonGoUtils,
			CpMultiplierMap cpMultiplierMap) {
		this.pokemonGoUtils = pokemonGoUtils;
		this.cpMultiplierMap = cpMultiplierMap;
	}

	@Override
	public void exec(SearchValue sv, WildIvResponse res) {
		GoPokedex gp = sv.getGoPokedex();
		int cp = ((Integer) sv.get(ParamsEnum.cp)).intValue();
		boolean wbFlg = ((Boolean) sv.get(ParamsEnum.wbFlg)).booleanValue();

		List<Map.Entry<String, Double>> rangeList = subListByRange(gp, cp, wbFlg);

		if (rangeList == null) {
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage(CP_OUT_OF_SCOPE_MSG);
			return;
		}

		List<VersatilityIv> ivList = getIvList(gp, cp, rangeList, wbFlg);
		res.setIvList(ivList);

		if (ivList.isEmpty()) {
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage(NO_HIT_MSG);
		}

		res.setCp(cp);
		res.setWbFlg(wbFlg);
	}

	private List<Map.Entry<String, Double>> subListByRange(GoPokedex gp, int cp, boolean wbFlg) {

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
	 * そのCPがありうるPLの範囲における、最大のPLを求めます。
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

	private int calcMaxIvIdx(GoPokedex gp, int cp, boolean wbFlg) {

		// 最高個体値の場合にcalcedCp < cpとなるPLの最低PLのインデックスを取得する。
		// （つまりは、そのポケモンそのCPにおいて、ありうる最低のPLを特定している。）
		int idx = pokemonGoUtils.binarySearchForPlIdx(
				(calcedCp) -> calcedCp < cp,
				(mult) -> pokemonGoUtils.calcCp(gp, MAX_IV, MAX_IV, MAX_IV, mult));

		return idx;
	}

	private List<VersatilityIv> getIvList(GoPokedex gp, int cp, List<Map.Entry<String, Double>> rangeList, boolean wbFlg) {

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
