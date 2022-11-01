package jp.brainjuice.pokego.business.service.research;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.business.service.utils.memory.TypeMap;
import jp.brainjuice.pokego.web.form.res.elem.Color;
import jp.brainjuice.pokego.web.form.res.research.RaceResponse;

@Service
public class RaceResearchService implements ResearchService<RaceResponse> {

	private PokedexRepository pokedexRepository;

	private GoPokedexRepository goPokedexRepository;

	private PokemonStatisticsInfo pokemonStatisticsInfo;

	private TypeMap typeMap;

	@Autowired
	public RaceResearchService(
			PokedexRepository pokedexRepository,
			GoPokedexRepository goPokedexRepository,
			PokemonStatisticsInfo pokemonStatisticsInfo,
			TypeMap typeMap) {
		this.pokedexRepository = pokedexRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonStatisticsInfo = pokemonStatisticsInfo;
		this.typeMap = typeMap;
	}

	@Override
	public void exec(IndividialValue iv, RaceResponse res) {

		String pokedexId = iv.getGoPokedex().getPokedexId();

		Pokedex pokedex = pokedexRepository.findById(pokedexId).get();
		GoPokedex goPokedex = goPokedexRepository.findById(pokedexId).get();

		res.setPokedex(pokedex);
		res.setGoPokedex(goPokedex);

		// タイプ1の色を設定
		Map<String, Integer> colorMap1 = typeMap.get(pokedex.getType1());
		res.setType1Color(new Color(
				colorMap1.get(TypeMap.KeyElem.r.name()),
				colorMap1.get(TypeMap.KeyElem.g.name()),
				colorMap1.get(TypeMap.KeyElem.b.name())));
		// タイプ2の色を設定
		if (!StringUtils.isEmpty(goPokedex.getType2())) {
			Map<String, Integer> colorMap2 = typeMap.get(pokedex.getType2());
			res.setType2Color(new Color(
					colorMap2.get(TypeMap.KeyElem.r.name()),
					colorMap2.get(TypeMap.KeyElem.g.name()),
					colorMap2.get(TypeMap.KeyElem.b.name())));
		}

		// ポケモンの色（タイプから算出））
		res.setColor(getColor(goPokedex));

		res.setStatistics(pokemonStatisticsInfo.clone());
	}

	/**
	 * ポケモンのタイプの色を取得します。（タイプ２が存在する場合は中間の色を取得する。）
	 *
	 * @return
	 */
	private Color getColor(GoPokedex goPokedex) {

		Color color = null;

		String type1 = goPokedex.getType1();
		String type2 = goPokedex.getType2();
		if (StringUtils.isEmpty(type2)) {
			// タイプ１のみの場合。
			Map<String, Integer> colorMap = typeMap.get(type1);
			color = new Color(
					colorMap.get(TypeMap.KeyElem.r.name()),
					colorMap.get(TypeMap.KeyElem.g.name()),
					colorMap.get(TypeMap.KeyElem.b.name()));
		} else {
			// タイプ２がある場合。
			Map<String, Integer> colorMap1 = typeMap.get(type1);
			Map<String, Integer> colorMap2 = typeMap.get(type2);
			color = new Color(
					(colorMap1.get(TypeMap.KeyElem.r.name()) + colorMap2.get(TypeMap.KeyElem.r.name())) / 2,
					(colorMap1.get(TypeMap.KeyElem.g.name()) + colorMap2.get(TypeMap.KeyElem.g.name())) / 2,
					(colorMap1.get(TypeMap.KeyElem.b.name()) + colorMap2.get(TypeMap.KeyElem.b.name())) / 2);
		}

		return color;
	}

}
