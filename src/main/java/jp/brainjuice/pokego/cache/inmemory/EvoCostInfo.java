package jp.brainjuice.pokego.cache.inmemory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.cache.inmemory.dto.EvoCostType;
import jp.brainjuice.pokego.dao.jpa.EvolutionRepository;
import jp.brainjuice.pokego.dao.jpa.entity.Evolution;

@Component
public class EvoCostInfo {

	/** アメ個数のマップ（{@literal Map<EvoCostType, Map<String型のアメの個数, List<EvolutionEdge>>>}） */
	private LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>> candyMap;

	/** アメ以外の進化条件全てのマップ */
	private LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>> costMap;

	public EvoCostInfo(EvolutionRepository evolutionRepository) {
		init(evolutionRepository);
	}

	/**
	 * メモリに抱えている、アメ以外の進化条件全てのマップを取得する。
	 *
	 * @return {@literal Map<EvoCostType, Map<進化方法, List<EvolutionEdge>>>}）
	 */
	public LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>> getCandyMap() {
		return candyMap;
	}

	/**
	 * メモリに抱えている、進化方法ごとのポケモン進化のリストを取得する。
	 *
	 * @return {@literal Map<EvoCostType, Map<進化方法, List<EvolutionEdge>>>}）
	 */
	public LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>> getCostMap() {
		return costMap;
	}

	private void init(EvolutionRepository evolutionRepository) {

		List<Evolution> evolList = evolutionRepository.findAll();

		candyMap =  new LinkedHashMap<>();
		LinkedHashMap<String, List<Evolution>> candyTmpMap = evolList.stream()
				.filter(evo -> evo.getCandy() != 0)
				// List<Evolution> → Map<Integer, List<Evolution>>(アメ個数ごとのEvolution)
				.collect(Collectors.groupingBy(Evolution::getCandy))
				.entrySet().stream()
				.sorted((o1, o2) -> o1.getKey() - o2.getKey()) // アメ少ない順
				// Map<Integer, List<Evolution>> → Map<String, List<EvolutionEdge>>
				.collect(Collectors.toMap(
						entry -> String.valueOf(entry.getKey().intValue()) + "コ",
						entry -> {
							return entry.getValue().stream()
									.collect(Collectors.toList());
						},
						(a, b) -> a,
						LinkedHashMap::new));
		candyMap.put(EvoCostType.candy, candyTmpMap);

		costMap = new LinkedHashMap<>();
		// 進化に必要な進化アイテム
		costMap.put(EvoCostType.evolutionItems, createMap(evolList, Evolution::getEvolutionItems));
		// 進化に必要な相棒としてのアクション
		costMap.put(EvoCostType.buddy, createMap(evolList, Evolution::getBuddy));
		// 特殊な行動、特殊な条件
		costMap.put(EvoCostType.specialAction, createMap(evolList, Evolution::getSpecialAction));
		// ルアーモジュールを使用した進化条件
		costMap.put(EvoCostType.lureModules, createMap(evolList, Evolution::getLureModules));
		// 進化に必要な交換の条件
		costMap.put(EvoCostType.tradeEvolution, createMap(evolList, Evolution::getTradeEvolution));
	}

	private LinkedHashMap<String, List<Evolution>> createMap(List<Evolution> evoList, Function<Evolution, String> getter) {

		return evoList.stream()
				.filter(evo -> !StringUtils.isEmpty(getter.apply(evo))) // 対象の進化条件に絞り込む
				.collect(Collectors.groupingBy(getter::apply)) // 対象の進化条件をキーとしてgroup byする
				.entrySet().stream()
				.sorted(Map.Entry.comparingByKey()) // 並び替え
				// Map<String, List<Evolution>> → Map<String, List<Evolution>>
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> entry.getValue().stream().collect(Collectors.toList()),
						(a, b) -> a,
						LinkedHashMap::new));

	}

}
