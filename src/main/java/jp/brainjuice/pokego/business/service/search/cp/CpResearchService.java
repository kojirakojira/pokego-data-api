package jp.brainjuice.pokego.business.service.search.cp;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.search.utils.PokemonGoUtils;
import jp.brainjuice.pokego.cache.inmemory.CpMultiplierMap;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.cp.CpResponse;

@Service
public class CpResearchService implements ResearchService<CpResponse> {

	private PokemonGoUtils pokemonGoUtils;

	private CpMultiplierMap cpMultiplierMap;

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

		int iva = sv.get(ParamsEnum.iva, int.class);
		int ivd = sv.get(ParamsEnum.ivd, int.class);
		int ivh = sv.get(ParamsEnum.ivh, int.class);
		String pl = sv.get(ParamsEnum.pl, String.class);

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
