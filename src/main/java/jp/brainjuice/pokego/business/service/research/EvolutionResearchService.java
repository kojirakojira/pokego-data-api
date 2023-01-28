package jp.brainjuice.pokego.business.service.research;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
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

	private PokemonEditUtils pokemonEditUtils;

	@Autowired
	public EvolutionResearchService(
			EvolutionInfo evolutionInfo,
			TypeMap typeMap,
			GoPokedexRepository goPokedexRepository,
			PokemonEditUtils pokemonEditUtils) {
		this.evolutionInfo = evolutionInfo;
		this.typeMap = typeMap;
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonEditUtils = pokemonEditUtils;
	}

	@Override
	public void exec(IndividialValue iv, EvolutionResponse res) {

		String pokedexId = iv.getGoPokedex().getPokedexId();

		// 進化ツリーの取得
		final List<List<List<Hierarchy>>> hieList = evolutionInfo.getEvoTrees(pokedexId);
		// 別のすがた
		final List<String> anotherFormList = evolutionInfo.getAnotherFormList(pokedexId);
		// 別のすがたの進化前、進化後
		final Set<String> bfAfAotFormSet = new HashSet<>();
		{
			// 進化ツリー上のポケモンを直列化する。
			final Set<String> treePokeIdSet = new HashSet<>();
			hieList.forEach(tree -> tree.forEach(li -> li.forEach(h -> treePokeIdSet.add(h.getId()))));
			// 直列化したポケモンをループさせ、別のすがたをすべて取得する。
			final Set<String> aotPokeIdSet = new HashSet<>();
			treePokeIdSet.forEach(pokeId -> aotPokeIdSet.addAll(evolutionInfo.getAnotherFormList(pokeId)));
			// 別のすがたの進化前、進化後をすべて取得する。
			aotPokeIdSet.forEach(pokeId -> bfAfAotFormSet.addAll(evolutionInfo.getBfAfEvoList(pokeId)));
			// 進化ツリー上のポケモン、別のすがたと重複している場合は削除
			// ※分岐進化で、別リージョンのポケモンに進化する場合、ツリー上のポケモンが取得されるため。
			bfAfAotFormSet.removeAll(treePokeIdSet);
			bfAfAotFormSet.removeAll(anotherFormList);
		}

		/** Raceマップの作成（色をクライアント側に渡すため。） */
		// 図鑑IDをすべてリストに追加する。
		final Set<String> pokedexIdSet = new HashSet<>();
		hieList.forEach(tree -> tree.forEach(li -> li.forEach(h -> pokedexIdSet.add(h.getId()))));
		pokedexIdSet.addAll(anotherFormList);
		pokedexIdSet.addAll(bfAfAotFormSet);

		// GOステータスの取得
		final List<GoPokedex> goPokedexList = (List<GoPokedex>) goPokedexRepository.findAllById(pokedexIdSet);

		// Raceマップを作成
		final Map<String, Race> raceMap = new HashMap<>();
		goPokedexList.forEach(gp -> {
			raceMap.put(gp.getPokedexId(), new Race(null, gp, typeMap));
		});

		/** レスポンスのセット */
		res.setEvoTreeInfo(hieList);
		// 並び替えてセット
		anotherFormList.sort(pokemonEditUtils.getPokedexIdComparator());
		res.setAnotherForms(anotherFormList);
		// 並び替えてリストに変換してセット
		res.setBfAfAotForms(bfAfAotFormSet.stream().sorted(pokemonEditUtils.getPokedexIdComparator()).collect(Collectors.toList()));
		res.setRaceMap(raceMap);
	}
}
