package jp.brainjuice.pokego.business.service.others;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.evo.Hierarchy;
import jp.brainjuice.pokego.business.service.utils.memory.evo.EvolutionProvider;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import jp.brainjuice.pokego.web.form.res.others.EvolutionResponse;

@Service
public class EvolutionResearchService implements ResearchService<EvolutionResponse> {

	private EvolutionProvider evolutionProvider;

	private GoPokedexRepository goPokedexRepository;

	@Autowired
	public EvolutionResearchService(
			EvolutionProvider evolutionProvider,
			GoPokedexRepository goPokedexRepository) {
		this.evolutionProvider = evolutionProvider;
		this.goPokedexRepository = goPokedexRepository;
	}

	@Override
	public void exec(SearchValue sv, EvolutionResponse res) {

		String pokedexId = sv.getGoPokedex().getPokedexId();

		// 進化ツリーの取得
		List<List<List<Hierarchy>>> hieList = evolutionProvider.getEvoTrees(pokedexId);

		// 進化ツリー上のポケモンを直列化する。
		Set<String> treePokeIdSet = new LinkedHashSet<>();
		hieList.forEach(tree -> tree.forEach(li -> li.forEach(h -> treePokeIdSet.add(h.getId()))));

		// 進化ツリー全体に係る注釈
		List<String> evoTreeAnnoList = evolutionProvider.getEvoAnnotations(treePokeIdSet);

		// 別のすがた
		List<String> anotherFormList = evolutionProvider.getAnotherFormList(pokedexId);
		// 別のすがたの進化前、進化後
		Set<String> bfAfAotFormSet = makeBfAfAotFormSet(treePokeIdSet, anotherFormList);

		// Raceマップの作成（色をクライアント側に渡すため。）
		Map<String, Race> raceMap = makeRaceMap(hieList, anotherFormList, bfAfAotFormSet);



		/** レスポンスのセット */
		res.setPid(pokedexId);
		res.setEvoTreeInfo(evolutionProvider.convDispHierarchy(hieList));
		res.setEvoTreeAnnotations(evoTreeAnnoList);
		// 並び替えてセット
		anotherFormList.sort(PokemonEditUtils.getPokedexIdComparator());
		res.setAnotherForms(anotherFormList);
		// 並び替えてリストに変換してセット
		res.setBfAfAotForms(bfAfAotFormSet.stream().sorted(PokemonEditUtils.getPokedexIdComparator()).collect(Collectors.toList()));
		res.setRaceMap(raceMap);
	}

	/**
	 * 別のすがたの進化前、進化後を取得する。
	 *
	 * @param treePokeIdSet
	 * @param anotherFormList
	 * @return
	 */
	private Set<String> makeBfAfAotFormSet(Set<String> treePokeIdSet, List<String> anotherFormList) {

		Set<String> bfAfAotFormSet = new HashSet<>();

		// 直列化したポケモンをループさせ、進化ツリー上の別のすがたをすべて取得する。
		Set<String> treeAotPokeIdSet = new HashSet<>();
		treePokeIdSet.forEach(pokeId -> treeAotPokeIdSet.addAll(evolutionProvider.getAnotherFormList(pokeId)));

		// 進化ツリー上の別のすがたをすべて追加する。
		bfAfAotFormSet.addAll(treeAotPokeIdSet);
		// 進化ツリー上の別のすがたの進化前、進化後をすべて追加する。
		treeAotPokeIdSet.forEach(pokeId -> bfAfAotFormSet.addAll(evolutionProvider.getBfAfEvoList(pokeId)));
		// 進化ツリー上のポケモン、別のすがたと重複している場合は削除
		bfAfAotFormSet.removeAll(treePokeIdSet);
		bfAfAotFormSet.removeAll(anotherFormList);

		return bfAfAotFormSet;
	}

	/**
	 * 種族値のMapを返却する。
	 * ここでの使用目的は、タイプから算出した色の情報が欲しいだけ。
	 *
	 * @param hieList
	 * @param anotherFormList
	 * @param bfAfAotFormSet
	 * @return
	 */
	private Map<String, Race> makeRaceMap(List<List<List<Hierarchy>>> hieList, List<String> anotherFormList, Set<String> bfAfAotFormSet) {

		// 図鑑IDをすべてリストに追加する。
		Set<String> pokedexIdSet = new HashSet<>();
		hieList.forEach(tree -> tree.forEach(li -> li.forEach(h -> pokedexIdSet.add(h.getId()))));
		pokedexIdSet.addAll(anotherFormList);
		pokedexIdSet.addAll(bfAfAotFormSet);

		// GOステータスの取得
		List<GoPokedex> goPokedexList = (List<GoPokedex>) goPokedexRepository.findAllById(pokedexIdSet);

		return goPokedexList.stream()
				.map(gp -> Map.entry(gp.getPokedexId(), new Race(null, gp)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
