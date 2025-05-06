package jp.brainjuice.pokego.business.service.search.catchCp;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.catchCp.utils.CatchCpUtils;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.utils.dto.cpIv.DynamaxIvRange;
import jp.brainjuice.pokego.business.service.search.utils.dto.cpIv.IvRangeCp;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.catchCp.DynamaxResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.CatchCp;

@Service
public class DynamaxResearchService implements ResearchService<DynamaxResponse> {

	private CatchCpUtils catchCpUtils;

	public DynamaxResearchService(CatchCpUtils catchCpUtils) {
		this.catchCpUtils = catchCpUtils;
	}

	@Override
	public void exec(SearchValue sv, DynamaxResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();

		if (!goPokedex.isDynamaxImplFlg() && !goPokedex.isGigantamaxImplFlg()) {
			res.setMessage("ダイマックス、キョダイマックス未実装のポケモンが指定されました。");
			res.setMsgLevel(MsgLevelEnum.error);
			res.setSuccess(false);
			return;
		}

		IvRangeCp baseRangeCp = catchCpUtils.getIvRangeCp(goPokedex, new DynamaxIvRange());
		CatchCp baseCatchCp = new CatchCp(baseRangeCp, null);
		
		if (goPokedex.isDynamaxImplFlg()) {
			// ダイマックス実装済みの場合
			res.setDynamaxGp(goPokedex);
			res.setDynamaxCatchCp(baseCatchCp);
		}
		
		if (goPokedex.isGigantamaxImplFlg()) {
			// キョダイマックス実装済みの場合
			res.setGigantamaxGp(goPokedex);
			res.setGigantamaxCatchCp(baseCatchCp);
		}
	}

}
