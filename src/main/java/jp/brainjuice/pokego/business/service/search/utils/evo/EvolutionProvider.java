package jp.brainjuice.pokego.business.service.search.utils.evo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.service.search.utils.dto.evo.Hierarchy;
import jp.brainjuice.pokego.cache.inmemory.EvoCostInfo;
import jp.brainjuice.pokego.cache.inmemory.dto.EvoCostType;
import jp.brainjuice.pokego.dao.jpa.entity.Evolution;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;

/**
 * Evolution系機能に参照するためのProviderクラス
 *
 * @author saibabanagchampa
 *
 */
@Component
public class EvolutionProvider {

	private EvolutionUtility evolutionUtility;

	private EvolutionInfo evolutionInfo;

	private EvoCostInfo evoCostInfo;

	public EvolutionProvider(
			EvolutionUtility evolutionUtility,
			EvolutionInfo evolutionInfo,
			EvoCostInfo evoCostInfo) {
		this.evolutionUtility = evolutionUtility;
		this.evolutionInfo = evolutionInfo;
		this.evoCostInfo = evoCostInfo;
	}

	/**
	 * この{@link EvolutionInfo#getAfterEvolution(String) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<String> getAfterEvolution(String pid) {
		return evolutionInfo.getAfterEvolution(pid);
	}

	/**
	 * この{@link EvolutionInfo#getAllAfterEvolution(String, List) メソッド}を参照
	 * @param pid
	 * @param evolTreeList
	 * @return
	 */
	public List<String> getAllAfterEvolution(String pid, List<Evolution> evolTreeList) {
		return evolutionInfo.getAllAfterEvolution(pid, evolTreeList);
	}

	/**
	 * この{@link EvolutionInfo#getAnotherFormList(String) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<String> getAnotherFormPidList(String pid) {
		return evolutionInfo.getAnotherFormPidList(pid);
	}

	/**
	 * この{@link EvolutionInfo#getAnotherFormListIn(String) メソッド}を参照
	 * @param pids
	 * @return
	 */
	public List<Evolution> getAnotherFormListIn(List<String> pids) {
		return evolutionInfo.getAnotherFormListIn(pids);
	}

	/**
	 * この{@link EvolutionInfo#basePokedexNo(String) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public int basePokedexNo(String pid) {
		return evolutionInfo.basePokedexNo(pid);
	}

	/**
	 * この{@link EvolutionInfo#isAfterEvolution(String) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public boolean isAfterEvolution(String pid) {
		return evolutionInfo.isAfterEvolution(pid);
	}

	/**
	 * この{@link EvolutionInfo#getRoot(String) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<String> getRoot(String pid) {
		return evolutionInfo.getRoot(pid);
	}

	/**
	 * この{@link EvolutionInfo#getRoot(String, List) メソッド}を参照
	 * @param pid
	 * @param lineageList
	 * @return
	 */
	public List<String> getRoot(String pid, List<Evolution> lineageList) {
		return evolutionInfo.getRoot(pid, lineageList);
	}

	/**
	 * この{@link EvolutionInfo#getLeaf(String) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<String> getLeaf(String pid) {
		return evolutionInfo.getLeaf(pid);
	}

	/**
	 * この{@link EvolutionInfo#getLeafCanGoEvol(String) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<String> getLeafCanGoEvol(String pid) {
		return evolutionInfo.getLeafCanGoEvol(pid);
	}

	/**
	 * この{@link EvolutionInfo#getLeafCanGoEvol(String, List) メソッド}を参照
	 * @param pid
	 * @param evolTreeLista
	 * @return
	 */
	public List<String> getLeafCanGoEvol(String pid, List<Evolution> evolTreeList) {
		return evolutionInfo.getLeafCanGoEvol(pid, evolTreeList);
	}

	/**
	 * この{@link EvoCostInfo#getCandyMap() メソッド}を参照
	 * @param pid
	 * @return
	 */
	public LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>>  getCandyMap() {
		return evoCostInfo.getCandyMap();
	}

	/**
	 * この{@link EvoCostInfo#getCostMap() メソッド}を参照
	 * @param pid
	 * @return
	 */
	public LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>>  getCostMap() {
		return evoCostInfo.getCostMap();
	}

	/**
	 * この{@link EvolutionUtility#getCosts(Evolution, Set, boolean) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<String> getCosts(Evolution evo, Set<EvoCostType> exclusionTypeSet, boolean implFlg) {
		return evolutionUtility.getCosts(evo, exclusionTypeSet, implFlg);
	}

	/**
	 * この{@link EvolutionInfo#getEvoTrees(String) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<List<List<Hierarchy>>> getEvoTrees(String pid) {
		return evolutionInfo.getEvoTrees(pid);
	}

	/**
	 * この{@link EvolutionInfo#getEvoTrees(List, List) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<List<List<Hierarchy>>> getEvoTrees(List<Evolution> evolList, List<GoPokedex> gpList) {
		return evolutionInfo.getEvoTrees(evolList, gpList);
	}

	/**
	 * この{@link EvolutionInfo#convDispHierarchy(List) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<List<List<Hierarchy>>> convDispHierarchy(List<List<List<Hierarchy>>> hieList) {
		return evolutionInfo.convDispHierarchy(hieList);
	}

	/**
	 * この{@link EvolutionUtility#getEvolAnnotations(Collection) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<String> getEvolAnnotations(Collection<String> pids) {
		return evolutionUtility.getEvolAnnotations(pids);
	}

	/**
	 * この{@link EvolutionInfo#getBfAfEvoList(String) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<String> getBfAfEvoList(String pid) {
		return evolutionInfo.getBfAfEvoList(pid);
	}

	/**
	 * この{@link EvolutionInfo#getLineageList(String) メソッド}を参照
	 * @param goPokedex
	 * @return
	 */
	public List<Evolution> getLineageList(GoPokedex goPokedex) {
		return evolutionInfo.getLineageList(goPokedex);
	}
	
	/**
	 * この{@link EvolutionInfo#getEvoTreeAndMegaList(String) メソッド}を参照
	 * @param pid
	 * @return
	 */
	public List<Evolution> getEvolTreeAndMegaList(String pid) {
		return evolutionInfo.getEvolTreeAndMegaList(pid);
	}
}
