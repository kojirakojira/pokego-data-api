package jp.brainjuice.pokego.business.service.research.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.memory.CpMultiplierMap;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.research.cp.CpResponse;

@Service
public class CpResearchService implements ResearchService<CpResponse> {

	private PokemonGoUtils pokemonGoUtils;

	private CpMultiplierMap cpMultiplierMap;

	@Autowired
	public CpResearchService(
			PokemonGoUtils pokemonGoUtils,
			CpMultiplierMap cpMultiplierMap) {
		this.pokemonGoUtils = pokemonGoUtils;
		this.cpMultiplierMap = cpMultiplierMap;
	}

	public boolean check(String pl, CpResponse cpResponse) {

		cpResponse.setSuccess(true);

		if (!cpMultiplierMap.containsKey(pl)) {
			cpResponse.setMessage("正しくないPLが指定されました。");
			cpResponse.setMsgLevel(MsgLevelEnum.error);
			cpResponse.setSuccess(false);
		}

		return cpResponse.isSuccess();
	}

	/**
	 * CPを算出します。
	 */
	@Override
	public void exec(SearchValue sv, CpResponse cpResponse) {

		int iva = ((Integer) sv.get(ParamsEnum.iva)).intValue();
		int ivd = ((Integer) sv.get(ParamsEnum.ivd)).intValue();
		int ivh = ((Integer) sv.get(ParamsEnum.ivh)).intValue();
		String pl = (String) sv.get(ParamsEnum.pl);

		cpResponse.setGoPokedex(sv.getGoPokedex());
		cpResponse.setIva(iva);
		cpResponse.setIvd(ivd);
		cpResponse.setIvh(ivh);
		cpResponse.setPl(pl);

		int cp = pokemonGoUtils.calcCp(sv.getGoPokedex(), iva, ivd, ivh, pl);

		cpResponse.setCp(cp);

		cpResponse.setMessage("");
	}

}
