package jp.brainjuice.pokego.business.service.utils.memory.evo;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.dto.evo.Evolution;
import jp.brainjuice.pokego.utils.BjCsvMapper;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class EvolutionList extends ArrayList<Evolution> {

	static final String ROOT = "root";

	private static final String FILE_NAME = "pokemon/pokemon-evolution.csv";

	private static final String NOT_EXISTS_MSG = "pokemon.csvに定義したポケモンがpokemon-evolution.csvに定義されていません。{0}";

	private static final String COSTS_CANDY_MSG = "アメ{0}個";

	private static final String COSTS_BUDDY_MSG = "相棒に設定し、{0}";

	private static final String UNIMPL_UNKNOWN_MSG = "未実装のため不明";

	@Autowired
	EvolutionList(GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {
		init(goPokedexRepository);
	}

	/**
	 * 進化条件をリスト形式で取得する。
	 *
	 * @param id 図鑑ID
	 * @param bid 進化前ポケモンの図鑑ID
	 * @return
	 */
	List<String> getCosts(String id, String bid) {

		if (ROOT.equals(bid)) {
			// rootの場合は進化前は存在しない。
			return new ArrayList<>();
		}

		// 対象のEvolutionを取得
		Evolution evo = this.stream()
				.filter(ev -> Objects.equals(ev.getPokedexId(), id) && Objects.equals(ev.getBeforePokedexId(), bid))
				.findFirst().get();

		return getCosts(evo, null);
	}

	/**
	 * 進化条件をリスト形式で取得する。
	 *
	 * @param evo
	 * @return
	 */
	List<String> getCosts(Evolution evo, Set<EvoCostType> exclusionTypeSet) {

		List<String> retList = new ArrayList<>();

		if (!evo.isImplFlg()) {
			// 未実装だった場合
			retList.add(UNIMPL_UNKNOWN_MSG);
			return retList;
		}

		// 除外対象かを確認する関数(true -> 除外対象)
		Predicate<EvoCostType> validExclusionFunc = (ect) -> exclusionTypeSet != null && !exclusionTypeSet.contains(ect);

		// 進化アイテム
		if (validExclusionFunc.test(EvoCostType.evolutionItems)) {
			BjUtils.addList(evo.getEvolutionItems(), retList);
		}

		// 相棒としてのアクション
		if (validExclusionFunc.test(EvoCostType.buddy)) {
			BjUtils.addList(
					evo.getBuddy(),
					retList,
					(str) -> MessageFormat.format(COSTS_BUDDY_MSG, str));
		}

		// ルアーモジュール
		if (validExclusionFunc.test(EvoCostType.lureModules)) {
			BjUtils.addList(evo.getLureModules(), retList);
		}

		// 交換
		if (validExclusionFunc.test(EvoCostType.tradeEvolution)) {
			BjUtils.addList(evo.getTradeEvolution(), retList);
		}

		// 特殊な条件
		if (validExclusionFunc.test(EvoCostType.specialAction)) {
			BjUtils.addList(evo.getSpecialAction(), retList);
		}

		// アメ
		if (validExclusionFunc.test(EvoCostType.candy)) {
			BjUtils.addList(
					evo.getCandy() == 0 ? "" : String.valueOf(evo.getCandy()), // アメ0個は、アメが進化条件にない、または未入力である。
							retList,
							(str) -> MessageFormat.format(COSTS_CANDY_MSG, str));
		}

		return retList;
	}

	/**
	 * その進化ツリー上のすべての注釈（evoAnnotation）を取得する。
	 * @param pids
	 * @return
	 */
	List<String> getEvoAnnotations(Collection<String> pids) {

		List<String> retList = new ArrayList<>();
		for (String pid: pids) {
			for (Evolution evo: this) {
				if (pid.equals(evo.getPokedexId()) && !evo.getEvoAnnotations().isEmpty()) {
					retList.add(evo.getEvoAnnotations());
					break;
				}
			}
		}
		return retList;
	}

	/**
	 * ポケモンGOにおいて進化できるかどうかを取得する。
	 *
	 * @param id
	 * @param bid
	 * @return
	 */
	boolean canGoEvo(String id, String bid) {

		if (ROOT.equals(bid)) {
			// rootの場合は進化前は存在しない。
			return false;
		}

		// 対象のEvolutionを取得
		Evolution evo = this.stream()
				.filter(ev -> Objects.equals(ev.getPokedexId(), id) && Objects.equals(ev.getBeforePokedexId(), bid))
				.findFirst().get();

		return evo.isCanGoEvo();
	}

	/**
	 * 起動時に実行。
	 *
	 * @throws PokemonDataInitException
	 */
	private void init(GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {

		try {
			List<Evolution> evolutionList = BjCsvMapper.mapping(FILE_NAME, Evolution.class);

			// pokemon.csvに定義したポケモンが、すべてpokemon-evolution.csvに定義されていることを確認する。
			checkAllExists(evolutionList, goPokedexRepository);

			// EvolutionにimplFlgをセット
			goPokedexRepository.findAll().stream().forEach(gp -> {
				for (Evolution evo: evolutionList) {
					if (gp.getPokedexId().equals(evo.getPokedexId())) {
						evo.setImplFlg(gp.isImplFlg());
						break;
					}
				}
			});
			// フィールドにEvolutionのリストをセット
			addAll(evolutionList);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}
	}

	/**
	 * pokemon.csvに定義したポケモンが、すべてpokemon-evolution.csvに定義されていることを確認する。
	 *
	 * @param evoList
	 * @param goPokedexRepository
	 * @return
	 * @throws PokemonDataInitException
	 */
	private void checkAllExists(List<Evolution> evoList, GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {

		List<String> evoPidList = evoList.stream()
				.map(Evolution::getPokedexId)
				.collect(Collectors.toList());

		List<GoPokedex> notExistsGpList = goPokedexRepository.findAll().stream()
				.filter(gp -> !evoPidList.contains(gp.getPokedexId()))
				.collect(Collectors.toList());

		// GoPokedexリストに存在していて、Evolutionリストに存在していないポケモンがいるかどうか。
		if (!notExistsGpList.isEmpty()) {
			throw new PokemonDataInitException(
					MessageFormat.format(
							NOT_EXISTS_MSG,
							notExistsGpList.stream().map(PokemonEditUtils::appendRemarks).collect(Collectors.toList())));
		}
	}
}
