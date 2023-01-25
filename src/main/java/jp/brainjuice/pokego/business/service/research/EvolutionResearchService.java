package jp.brainjuice.pokego.business.service.research;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.dto.Hierarchy;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.memory.EvolutionInfo;
import jp.brainjuice.pokego.business.service.utils.memory.TypeMap;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import jp.brainjuice.pokego.web.form.res.research.EvolutionResponse;

@Service
public class EvolutionResearchService implements ResearchService<EvolutionResponse> {

	private EvolutionInfo evolutionInfo;

	private TypeMap typeMap;

	private GoPokedexRepository goPokedexRepository;

	@Autowired
	public EvolutionResearchService(EvolutionInfo evolutionInfo, TypeMap typeMap, GoPokedexRepository goPokedexRepository) {
		this.evolutionInfo = evolutionInfo;
		this.typeMap = typeMap;
		this.goPokedexRepository = goPokedexRepository;
	}

	@Override
	public void exec(IndividialValue iv, EvolutionResponse res) {

		String pokedexId = iv.getGoPokedex().getPokedexId();

		// 進化ツリーの取得
		final List<List<List<Hierarchy>>> hieList = evolutionInfo.getEvoTrees(pokedexId);
		// 別のすがた
		final List<String> anotherFormList = evolutionInfo.getAnotherFormList(pokedexId);

		/** Raceマップの作成（色をクライアント側に渡すため。） */
		// 図鑑IDをすべてリストに追加する。
		final Set<String> pokedexIdList = new HashSet<>();
		hieList.forEach(tree -> tree.forEach(li -> li.forEach(h -> pokedexIdList.add(h.getId()))));
		pokedexIdList.addAll(anotherFormList);

		// GOステータスの取得
		final List<GoPokedex> goPokedexList = (List<GoPokedex>) goPokedexRepository.findAllById(pokedexIdList);

		// Raceマップを作成
		final Map<String, Race> raceMap = new HashMap<>();
		goPokedexList.forEach(gp -> {
			raceMap.put(gp.getPokedexId(), new Race(null, gp, typeMap));
		});

		// レスポンスのセット
		res.setEvoTreeInfo(hieList);
		res.setAnotherForms(anotherFormList);
		res.setRaceMap(raceMap);
	}

}
