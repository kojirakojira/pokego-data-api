package jp.brainjuice.pokego.business.service.others;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.dto.evo.Evolution;
import jp.brainjuice.pokego.business.service.utils.memory.evo.EvoCostType;
import jp.brainjuice.pokego.business.service.utils.memory.evo.EvolutionProvider;
import jp.brainjuice.pokego.web.form.res.elem.EvolutionEdge;
import jp.brainjuice.pokego.web.form.res.others.EvoCostResponse;

@Service
public class EvoCostResearchService {

	private EvolutionProvider evolutionProvider;

	private GoPokedexRepository goPokedexRepository;

	public enum Costs {
		/** アメの一覧を表示させたい場合 */
		candy,
		/** アメ以外の進化アイテム等の進化条件の一覧を表示させたい場合 */
		othrCosts
	}

	@Autowired
	public EvoCostResearchService(
			EvolutionProvider evolutionProvider,
			GoPokedexRepository goPokedexRepository) {
		this.evolutionProvider = evolutionProvider;
		this.goPokedexRepository = goPokedexRepository;
	}

	public void exec(Costs costs, EvoCostResponse res) {

		LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>> costsMap = switch (costs) {
		case candy -> evolutionProvider.getCandyMap();
		case othrCosts ->  evolutionProvider.getCostMap();
		};

		// EvoCostTypeを日本語に変換。Evolution → EvolutionEdgeGoPokedexに変換。
		LinkedHashMap<String, LinkedHashMap<String, List<EvolutionEdge>>> costsGpMap = costsMap.entrySet().stream()
				.collect(Collectors.toMap(
						entry -> entry.getKey().getJpn(),
						entry -> {
							return entry.getValue().entrySet().stream()
									.collect(Collectors.toMap(
											Map.Entry::getKey,
											entry2 -> {
												List<Evolution> eeList = entry2.getValue();
												return eeList.stream()
														.map(ee -> convEvoEdge(ee, costs))
														.collect(Collectors.toList());
											},
											(a, b) -> a,
											LinkedHashMap::new));
						},
						(a, b) -> a,
						LinkedHashMap::new));

		res.setCandy(costs == Costs.candy);
		res.setPoke(false);
		res.setCostTypeMap(costsGpMap);
	}

	private EvolutionEdge convEvoEdge(Evolution evo, Costs costs) {

		String pid = evo.getPokedexId();
		String bPid = evo.getBeforePokedexId();
		GoPokedex gp = goPokedexRepository.findById(pid).orElseThrow(() -> new RuntimeException(pid));
		GoPokedex bGp = goPokedexRepository.findById(bPid).orElseThrow(() -> new RuntimeException(pid));
		List<String> annoList = costs == Costs.candy ? evolutionProvider.getCosts(evo, Set.of(EvoCostType.candy)) : new ArrayList<>();

		EvolutionEdge ee = new EvolutionEdge(pid, bPid, gp, bGp, annoList);

		return ee;
	}
}
