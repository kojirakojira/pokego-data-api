package jp.brainjuice.pokego.business.service.utils.evo;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.EvolutionRepository;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.Evolution;
import jp.brainjuice.pokego.business.dao.entity.EvolutionPk;
import jp.brainjuice.pokego.cache.inmemory.dto.EvoCostType;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataException;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;

@Component
class EvolutionUtility {

	private GoPokedexRepository goPokedexRepository;

	private EvolutionRepository evolutionRepository;

	static final String ROOT = "root";

	private static final String COSTS_CANDY_MSG = "アメ{0}個";

	private static final String COSTS_BUDDY_MSG = "相棒に設定し、{0}";

	private static final String UNIMPL_UNKNOWN_MSG = "未実装のため不明";

	EvolutionUtility(
			GoPokedexRepository goPokedexRepository,
			EvolutionRepository evolutionRepository) throws PokemonDataInitException {
		this.goPokedexRepository = goPokedexRepository;
		this.evolutionRepository = evolutionRepository;
	}

	/**
	 * 進化条件をリスト形式で取得する。
	 *
	 * @param id 図鑑ID
	 * @param bid 進化前ポケモンの図鑑ID
	 * @return
	 */
	List<String> getCosts(String id, String bid) throws PokemonDataException {

		if (ROOT.equals(bid)) {
			// rootの場合は進化前は存在しない。
			return new ArrayList<>();
		}

		// 対象のEvolutionを取得
		Evolution evo = evolutionRepository.findById(new EvolutionPk(id, bid)).orElseThrow(PokemonDataException::new);
		boolean implFlg = goPokedexRepository.findImplFlgById(evo.getPokedexId());

		return getCosts(evo, null, implFlg);
	}

	/**
	/**
	 * 進化条件をリスト形式で取得する。
	 * @param evo
	 * @param implFlg
	 * @return
	 * @throws PokemonDataException
	 */
	List<String> getCosts(Evolution evo, boolean implFlg) throws PokemonDataException {

		if (ROOT.equals(evo.getBeforePokedexId())) {
			// rootの場合は進化前は存在しない。
			return new ArrayList<>();
		}

		return getCosts(evo, null, implFlg);
	}

	/**
	 * 進化条件をリスト形式で取得する。
	 * 
	 * @param evo
	 * @param exclusionTypeSet
	 * @param implFlg
	 * @return
	 */
	List<String> getCosts(Evolution evo, Set<EvoCostType> exclusionTypeSet, boolean implFlg) {

		List<String> retList = new ArrayList<>();

		if (!implFlg) {
			// 未実装だった場合
			retList.add(UNIMPL_UNKNOWN_MSG);
			return retList;
		}

		// 除外対象かを確認する関数(true -> 除外対象。第二引数がnullの場合はスルー)
		Predicate<EvoCostType> validExclusionFunc = (ect) -> exclusionTypeSet == null || !exclusionTypeSet.contains(ect);

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
	 * その進化ツリー上のすべての注釈（evolAnnotation）を取得する。
	 * @param pids
	 * @return
	 */
	List<String> getEvolAnnotations(Collection<String> pids) {

		return evolutionRepository.findEvolAnnotationsByIdIn(pids);
	}

	/**
	 * ポケモンGOにおいて進化できるかどうかを取得する。
	 *
	 * @param id
	 * @param bid
	 * @return
	 */
	boolean canGoEvo(Evolution evol) throws PokemonDataException {

		if (ROOT.equals(evol.getBeforePokedexId())) {
			// rootの場合は進化前は存在しない。
			return false;
		}

		return evol.isCanGoEvol();
	}

	/**
	 * ポケモンGOにおいて進化できるかどうかを取得する。
	 *
	 * @param id
	 * @param bid
	 * @return
	 */
	boolean canGoEvo(String id, String bid) throws PokemonDataException {

		if (ROOT.equals(bid)) {
			// rootの場合は進化前は存在しない。
			return false;
		}

		// 対象のEvolutionを取得
		Evolution evo = evolutionRepository.findById(new EvolutionPk(id, bid)).orElseThrow(PokemonDataException::new);

		return evo.isCanGoEvol();
	}
}
