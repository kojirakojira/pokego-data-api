package jp.brainjuice.pokego.business.service.research.cp;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.WildIvUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
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

	WildIvUtils wildIvUtils;

	private static final String NO_HIT_MSG = "該当する個体値が存在しませんでした。";

	private static final String CP_OUT_OF_SCOPE_MSG = "野生ではありえないCPが指定されました。";

	@Autowired
	public WildIvResearchService(
			WildIvUtils wildIvUtils) {
		this.wildIvUtils = wildIvUtils;
	}

	@Override
	public void exec(SearchValue sv, WildIvResponse res) {
		GoPokedex gp = sv.getGoPokedex();
		int cp = ((Integer) sv.get(ParamsEnum.cp)).intValue();
		boolean wbFlg = ((Boolean) sv.get(ParamsEnum.wbFlg)).booleanValue();

		List<Map.Entry<String, Double>> rangeList = wildIvUtils.subListByRange(gp, cp, wbFlg);

		if (rangeList == null) {
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage(CP_OUT_OF_SCOPE_MSG);
			return;
		}

		List<VersatilityIv> ivList = wildIvUtils.getIvList(gp, cp, rangeList, wbFlg);
		res.setIvList(ivList);

		if (ivList.isEmpty()) {
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage(NO_HIT_MSG);
		}

		res.setCp(cp);
		res.setWbFlg(wbFlg);
	}

}