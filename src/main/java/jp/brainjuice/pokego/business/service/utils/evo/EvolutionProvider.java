package jp.brainjuice.pokego.business.service.utils.evo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.entity.Evolution;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.dto.evo.Hierarchy;
import jp.brainjuice.pokego.cache.inmemory.EvoCostInfo;
import jp.brainjuice.pokego.cache.inmemory.dto.EvoCostType;

/**
 * Evolution系機能に参照するためのProviderクラス
 *
 * @author saibabanagchampa
 *
 */
@Component
public class EvolutionProvider {

	private EvolutionUtility evolutionList;

	private EvolutionInfo evolutionInfo;

	private EvoCostInfo evoCostInfo;

	public EvolutionProvider(
			EvolutionUtility evolutionList,
			EvolutionInfo evolutionInfo,
			EvoCostInfo evoCostInfo) {
		this.evolutionList = evolutionList;
		this.evolutionInfo = evolutionInfo;
		this.evoCostInfo = evoCostInfo;
	}

	public List<String> getAfterEvolution(String pid) {
		return evolutionInfo.getAfterEvolution(pid);
	}

	public List<String> getAnotherFormList(String pid) {
		return evolutionInfo.getAnotherFormList(pid);
	}

	public int basePokedexNo(String pid) {
		return evolutionInfo.basePokedexNo(pid);
	}

	public boolean isAfterEvolution(String pid) {
		return evolutionInfo.isAfterEvolution(pid);
	}

	public List<String> getRoot(String pid) {
		return evolutionInfo.getRoot(pid);
	}

	public List<String> getLeaf(String pid) {
		return evolutionInfo.getLeaf(pid);
	}

	public LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>>  getCandyMap() {
		return evoCostInfo.getCandyMap();
	}

	public LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>>  getCostMap() {
		return evoCostInfo.getCostMap();
	}

	public List<String> getCosts(Evolution evo, Set<EvoCostType> exclusionTypeSet, boolean implFlg) {
		return evolutionList.getCosts(evo, exclusionTypeSet, implFlg);
	}

	public List<List<List<Hierarchy>>> getEvoTrees(String pid) {
		return evolutionInfo.getEvoTrees(pid);
	}

	public List<List<List<Hierarchy>>> getEvoTrees(List<Evolution> evolList, List<GoPokedex> gpList) {
		return evolutionInfo.getEvoTrees(evolList, gpList);
	}

	public List<List<List<Hierarchy>>> convDispHierarchy(List<List<List<Hierarchy>>> hieList) {
		return evolutionInfo.convDispHierarchy(hieList);
	}

	public List<String> getEvolAnnotations(Collection<String> pids) {
		return evolutionList.getEvolAnnotations(pids);
	}

	public List<String> getBfAfEvoList(String pid) {
		return evolutionInfo.getBfAfEvoList(pid);
	}

	public List<Evolution> getLineageList(String pid) {
		return evolutionInfo.getLineageList(pid);
	}
}
