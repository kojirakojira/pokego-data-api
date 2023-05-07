package jp.brainjuice.pokego.business.service.research.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue.ParamsEnum;
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
	public void exec(IndividialValue iv, CpResponse cpResponse) {

		int iva = ((Integer) iv.get(ParamsEnum.iva)).intValue();
		int ivd = ((Integer) iv.get(ParamsEnum.ivd)).intValue();
		int ivh = ((Integer) iv.get(ParamsEnum.ivh)).intValue();
		String pl = (String) iv.get(ParamsEnum.pl);

		cpResponse.setGoPokedex(iv.getGoPokedex());
		cpResponse.setIva(iva);
		cpResponse.setIvd(ivd);
		cpResponse.setIvh(ivh);
		cpResponse.setPl(pl);

		int cp = pokemonGoUtils.calcCp(iv.getGoPokedex(), iva, ivd, ivh, pl);

		cpResponse.setCp(cp);

		cpResponse.setMessage("");
	}

}
