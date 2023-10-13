package jp.brainjuice.pokego.business.service.utils.memory.evo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.service.utils.dto.evo.Evolution;

@Component
class EvoCostInfo {

	/** アメ個数のマップ（{@literal Map<EvoCostType, Map<String型のアメの個数, List<EvolutionEdge>>>}） */
	private LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>> candyMap;

	/** アメ以外の進化条件全てのマップ */
	private LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>> costMap;

	@Autowired
	EvoCostInfo(EvolutionList evoList, EvoExceptionsMap exceptionsMap) {
		init(evoList, exceptionsMap);
	}

	/**
	 * メモリに抱えている、アメ以外の進化条件全てのマップを取得する。
	 *
	 * @return {@literal Map<EvoCostType, Map<進化方法, List<EvolutionEdge>>>}）
	 */
	LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>> getCandyMap() {
		return candyMap;
	}

	/**
	 * メモリに抱えている、進化方法ごとのポケモン進化のリストを取得する。
	 *
	 * @return {@literal Map<EvoCostType, Map<進化方法, List<EvolutionEdge>>>}）
	 */
	LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>> getCostMap() {
		return costMap;
	}

	private void init(EvolutionList evoList, EvoExceptionsMap exceptionsMap) {

		List<Evolution> exclusionEvoList = null;
		{
			// ダミーのポケモンのマップ
			List<String> exPidList = exceptionsMap.entrySet().stream()
					.flatMap(entry -> entry.getValue().stream())
					.collect(Collectors.toList());
			// ダミーのポケモンを排除したリスト
			exclusionEvoList = evoList.stream()
					.filter(evo -> !exPidList.contains(evo.getPokedexId()) && !exPidList.contains(evo.getBeforePokedexId()))
					.collect(Collectors.toList());
		}

		candyMap =  new LinkedHashMap<>();
		LinkedHashMap<String, List<Evolution>> candyTmpMap = exclusionEvoList.stream()
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
		costMap.put(EvoCostType.evolutionItems, createMap(exclusionEvoList, Evolution::getEvolutionItems));
		// 進化に必要な相棒としてのアクション
		costMap.put(EvoCostType.buddy, createMap(exclusionEvoList, Evolution::getBuddy));
		// 特殊な行動、特殊な条件
		costMap.put(EvoCostType.specialAction, createMap(exclusionEvoList, Evolution::getSpecialAction));
		// ルアーモジュールを使用した進化条件
		costMap.put(EvoCostType.lureModules, createMap(exclusionEvoList, Evolution::getLureModules));
		// 進化に必要な交換の条件
		costMap.put(EvoCostType.tradeEvolution, createMap(exclusionEvoList, Evolution::getTradeEvolution));
	}

	private LinkedHashMap<String, List<Evolution>> createMap(List<Evolution> evoList, Function<Evolution, String> getter) {

		return evoList.stream()
				.filter(evo -> !getter.apply(evo).isEmpty()) // 対象の進化条件に絞り込む
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
