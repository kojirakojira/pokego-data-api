package jp.brainjuice.pokego.business.service.research.cp;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.SituationEnum;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.CpIvCalculator;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.AllIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.EggIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.FrTaskIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RaidIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.WildIvRange;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.elem.VersatilityIv;
import jp.brainjuice.pokego.web.form.res.research.cp.CpIvResponse;

/**
 * CPからポケモンの個体値を算出するサービスクラス
 *
 * @author saibabanagchampa
 *
 */
@Service
public class CpIvResearchService implements ResearchService<CpIvResponse> {

	CpIvCalculator cpIvCalculator;

	private static final String NO_HIT_MSG = "該当する個体値が存在しませんでした。";

	private static final String CP_OUT_OF_SCOPE_MSG = "ありえないCPが指定されました。";

	@Autowired
	public CpIvResearchService(
			CpIvCalculator cpIvCalculator) {
		this.cpIvCalculator = cpIvCalculator;
	}

	@Override
	public void exec(SearchValue sv, CpIvResponse res) {

		GoPokedex gp = sv.getGoPokedex();
		int cp = ((Integer) sv.get(ParamsEnum.cp)).intValue();
		boolean wbFlg = ((Boolean) sv.get(ParamsEnum.wbFlg)).booleanValue();
		SituationEnum situation = SituationEnum.valueOf((String) sv.get(ParamsEnum.situation));

		// シチュエーションに応じたIvRangeの生成
		IvRange ir = switch (situation) {
		case wild -> new WildIvRange();
		case frTask -> new FrTaskIvRange();
		case egg -> new EggIvRange();
		case raid -> new RaidIvRange();
		case non -> new AllIvRange();
		default -> throw new IllegalArgumentException("Unexpected value: " + situation); // 起こらないパターン。
		};

		// 範囲内のPL:CpMultiplierのリストを生成。
		List<Map.Entry<String, Double>> rangeList = cpIvCalculator.subListByRange(gp, cp, wbFlg, ir);

		if (rangeList == null) {
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage(CP_OUT_OF_SCOPE_MSG);
			return;
		}

		List<VersatilityIv> ivList = cpIvCalculator.getIvList(gp, cp, rangeList, wbFlg, ir);
		res.setIvList(ivList);

		if (ivList.isEmpty()) {
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage(NO_HIT_MSG);
		}

		res.setCp(cp);
		res.setWbFlg(wbFlg);
	}

}
