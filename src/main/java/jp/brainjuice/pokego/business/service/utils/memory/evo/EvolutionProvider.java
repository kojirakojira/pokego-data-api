package jp.brainjuice.pokego.business.service.utils.memory.evo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.service.utils.dto.evo.Evolution;
import jp.brainjuice.pokego.business.service.utils.dto.evo.Hierarchy;

/**
 * Evolution系機能に参照するためのProviderクラス
 *
 * @author saibabanagchampa
 *
 */
@Component
public class EvolutionProvider {

	private EvolutionList evolutionList;

	private EvolutionInfo evolutionInfo;

	private EvoCostInfo evoCostInfo;

	@Autowired
	public EvolutionProvider(
			EvolutionList evolutionList,
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

	public String getFirstInEvoTree(String pid) {
		return evolutionInfo.getFirstInEvoTree(pid);
	}

	public List<String> getLastInEvoTree(String pid) {
		return evolutionInfo.getLastInEvoTree(pid);
	}

	public LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>>  getCandyMap() {
		return evoCostInfo.getCandyMap();
	}

	public LinkedHashMap<EvoCostType, LinkedHashMap<String, List<Evolution>>>  getCostMap() {
		return evoCostInfo.getCostMap();
	}

	public List<String> getCosts(Evolution evo, Set<EvoCostType> exclusionTypeSet) {
		return evolutionList.getCosts(evo, exclusionTypeSet);
	}

	public List<List<List<Hierarchy>>> getEvoTrees(String pid) {
		return evolutionInfo.getEvoTrees(pid);
	}

	public List<List<List<Hierarchy>>> convDispHierarchy(List<List<List<Hierarchy>>> hieList) {
		return evolutionInfo.convDispHierarchy(hieList);
	}

	public List<String> getEvoAnnotations(Collection<String> pids) {
		return evolutionList.getEvoAnnotations(pids);
	}

	public List<String> getBfAfEvoList(String pid) {
		return evolutionInfo.getBfAfEvoList(pid);
	}
}
