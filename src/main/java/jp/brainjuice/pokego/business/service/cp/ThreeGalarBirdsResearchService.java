package jp.brainjuice.pokego.business.service.cp;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.utils.CpIvCalculator;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.WildIvRange;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.cp.ThreeGalarBirdsResponse;
import jp.brainjuice.pokego.web.form.res.elem.VersatilityIv;

@Service
public class ThreeGalarBirdsResearchService implements ResearchService<ThreeGalarBirdsResponse> {

	CpIvCalculator cpIvCalculator;

	private static final String NO_HIT_MSG = "該当する個体値が存在しませんでした。";

	private static final String CP_OUT_OF_SCOPE_MSG = "野生ではありえないCPが指定されました。";

	private static final String NO_TGB_MSG = "ガラル三鳥を指定してください。";

	private List<String> tgbPidList = Arrays.asList("0144G01", "0145G01", "0146G01");

	@Autowired
	public ThreeGalarBirdsResearchService(
			CpIvCalculator cpIvCalculator) {
		this.cpIvCalculator = cpIvCalculator;
	}

	@Override
	public void exec(SearchValue sv, ThreeGalarBirdsResponse res) {
		GoPokedex gp = sv.getGoPokedex();

		if (!tgbPidList.contains(gp.getPokedexId())) {
			// ガラル三鳥以外のポケモンが指定された場合（URLをいじられた場合のみ起こる）
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage(NO_TGB_MSG);
			return;
		}

		int cp = ((Integer) sv.get(ParamsEnum.cp)).intValue();
		boolean wbFlg = ((Boolean) sv.get(ParamsEnum.wbFlg)).booleanValue();
		IvRange ir = new WildIvRange();

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
