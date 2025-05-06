package jp.brainjuice.pokego.business.service.search.cp;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.search.utils.CpIvCalculator;
import jp.brainjuice.pokego.business.service.search.utils.dto.cpIv.IvRange;
import jp.brainjuice.pokego.business.service.search.utils.dto.cpIv.WildIvRange;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.cp.ThreeGalarBirdsResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.VersatilityIv;

@Service
public class ThreeGalarBirdsResearchService implements ResearchService<ThreeGalarBirdsResponse> {
	
	private GoPokedexRepository goPokedexRepository;

	private CpIvCalculator cpIvCalculator;

	private static final String NO_HIT_MSG = "該当する個体値が存在しませんでした。";

	private static final String CP_OUT_OF_SCOPE_MSG = "ありえないCPが指定されました。";

	private static final String NO_TGB_MSG = "ガラル三鳥を指定してください。";

	private List<String> tgbPidList = Arrays.asList("0144G01", "0145G01", "0146G01");

	public ThreeGalarBirdsResearchService(
			GoPokedexRepository goPokedexRepository,
			CpIvCalculator cpIvCalculator) {
		this.cpIvCalculator = cpIvCalculator;
		this.goPokedexRepository = goPokedexRepository;
	}
	
	/**
	 * ガラル三鳥のGoPokedexのリストを取得する。
	 * @return
	 */
	public List<GoPokedex> getList() {
		return goPokedexRepository.findAllById(tgbPidList);
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

		int cp = sv.get(ParamsEnum.cp, int.class);
		boolean wbFlg = sv.get(ParamsEnum.wbFlg, boolean.class);
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
