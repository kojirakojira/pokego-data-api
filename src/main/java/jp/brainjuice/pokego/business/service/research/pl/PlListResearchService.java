package jp.brainjuice.pokego.business.service.research.pl;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.memory.CpMultiplierMap;
import jp.brainjuice.pokego.web.form.res.elem.PlCp;
import jp.brainjuice.pokego.web.form.res.research.pl.PlListResponse;

@Service
public class PlListResearchService implements ResearchService<PlListResponse> {

	private PokemonGoUtils pokemonGoUtils;

	private CpMultiplierMap cpMultiplierMap;

	public PlListResearchService(
			PokemonGoUtils pokemonGoUtils,
			CpMultiplierMap cpMultiplierMap) {
		this.pokemonGoUtils = pokemonGoUtils;
		this.cpMultiplierMap = cpMultiplierMap;
	}

	@Override
	public void exec(IndividialValue iv, PlListResponse res) {

		GoPokedex goPokedex = iv.getGoPokedex();

		ArrayList<PlCp> plList = new ArrayList<>();
		cpMultiplierMap.forEach((k, v) -> {
			int cp = pokemonGoUtils.calcCp(
					goPokedex,
					((Integer) iv.get(ParamsEnum.iva)).intValue(),
					((Integer) iv.get(ParamsEnum.ivd)).intValue(),
					((Integer) iv.get(ParamsEnum.ivh)).intValue(),
					k);
			// noは一旦nullで作成
			plList.add(new PlCp(null, k, cp));
		});

		// 並び替え
		Collections.sort(plList, (o1, o2) -> {
			return Double.parseDouble(o1.getPl()) < Double.parseDouble(o2.getPl()) ? -1 : 1;
		});

		// no採番
		for(int i = 0; i < plList.size(); i++) {
			plList.get(i).setNo(String.valueOf(i + 1));
		}

		res.setPlList(plList);
		res.setIva(((Integer) iv.get(ParamsEnum.iva)).intValue());
		res.setIvd(((Integer) iv.get(ParamsEnum.ivd)).intValue());
		res.setIvh(((Integer) iv.get(ParamsEnum.ivh)).intValue());
	}

}
