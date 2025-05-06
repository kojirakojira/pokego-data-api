package jp.brainjuice.pokego.business.service.search.others;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.evo.Hierarchy;
import jp.brainjuice.pokego.business.service.search.utils.evo.EvolutionProvider;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.Evolution;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.elem.Race;
import jp.brainjuice.pokego.web.search.form.res.others.EvolutionResponse;

@Service
public class EvolutionResearchService implements ResearchService<EvolutionResponse> {

	private EvolutionProvider evolutionProvider;

	private GoPokedexRepository goPokedexRepository;

	public EvolutionResearchService(
			EvolutionProvider evolutionProvider,
			GoPokedexRepository goPokedexRepository) {
		this.evolutionProvider = evolutionProvider;
		this.goPokedexRepository = goPokedexRepository;
	}

	@Override
	public void exec(SearchValue sv, EvolutionResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();
		String pokedexId = goPokedex.getPokedexId();

		// その系統のすべてのポケモンのEvolutionを取得する。
		List<Evolution> lineageList = evolutionProvider.getLineageList(goPokedex);

		// 別のすがた（そのポケモンのメガシンカ、別リージョン等）
		List<String> anotherFormList = evolutionProvider.getAnotherFormPidList(pokedexId);

		List<GoPokedex> goPokedexList = goPokedexRepository.findAllById(
				Stream.concat(
						lineageList.stream().map(Evolution::getPokedexId),
						anotherFormList.stream())
				.toList()
				);

		List<List<List<Hierarchy>>> lineageHieList = evolutionProvider.getEvoTrees(lineageList, goPokedexList);

		// 検索対象のポケモンを含む進化ツリーの取得
		List<List<List<Hierarchy>>> targetTreeHieList = getTargetTree(pokedexId, lineageHieList);

		// 進化ツリー上のポケモンを直列化する。
		Set<String> targetTreePidSet = serializeHierarchy(targetTreeHieList);

		// 進化ツリー全体に係る注釈
		List<String> evolTreeAnnoList = evolutionProvider.getEvolAnnotations(targetTreePidSet);

		// 別のすがたの進化前、進化後
		// その系統のすべてのポケモン - (検索対象のポケモンを含む進化ツリーのポケモン + 別のすがた)
		List<String> bfAfAotFormList = serializeHierarchy(lineageHieList).stream()
				.filter(lpid -> !targetTreePidSet.contains(lpid))
				.filter(lpid -> !anotherFormList.contains(lpid))
				.toList();

		// Raceマップの作成（色をクライアント側に渡すため。）
		Map<String, Race> raceMap = makeRaceMap(goPokedexList);

		/** レスポンスのセット */
		res.setPid(pokedexId);
		res.setEvolTreeInfo(evolutionProvider.convDispHierarchy(targetTreeHieList));
		res.setEvolTreeAnnotations(evolTreeAnnoList);
		// 並び替えてセット
		anotherFormList.sort(PokemonEditUtils.getPokedexIdComparator());
		res.setAnotherForms(anotherFormList);
		// 並び替えてリストに変換してセット
		res.setBfAfAotForms(bfAfAotFormList.stream().sorted(PokemonEditUtils.getPokedexIdComparator()).collect(Collectors.toList()));
		res.setRaceMap(raceMap);
	}

	/**
	 * 系統全体から、検索対象のpokedexIdが含まれるツリーを抜き出す。
	 * @param pokedexId
	 * @param lineageHieList
	 * @return
	 */
	private List<List<List<Hierarchy>>> getTargetTree(String pokedexId, List<List<List<Hierarchy>>> lineageHieList) {

		List<List<List<Hierarchy>>> hieList = new ArrayList<>();
		for (List<List<Hierarchy>> yList: lineageHieList) {
			boolean skipFlg = false;
			for (List<Hierarchy> xList: yList) {
				for (Hierarchy hie: xList) {
					if (hie.getId().equals(pokedexId)) {
						hieList.add(yList);
						skipFlg = true;
						break;
					}
				}

				if (skipFlg) {
					break;
				}
			}
		}
		return hieList;
	}

	private Set<String> serializeHierarchy(List<List<List<Hierarchy>>> hieList) {
		Set<String> pidSet = new HashSet<>();
		hieList.forEach(tree -> tree.forEach(li -> li.forEach(h -> pidSet.add(h.getId()))));
		return pidSet;
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
	private Map<String, Race> makeRaceMap(List<GoPokedex> goPokedexList) {

		return goPokedexList.stream()
				.map(gp -> Map.entry(gp.getPokedexId(), new Race(null, gp)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
