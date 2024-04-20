package jp.brainjuice.pokego.business.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.PokedexSpecifications.FilterEnum;
import jp.brainjuice.pokego.business.dao.dto.FilterParam;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonUtils;
import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * ポケモンGOにおけるポケモンの情報を取得するRepositoryクラス
 *
 * @author saibabanagchampa
 *
 */
@Repository
@Slf4j
public class GoPokedexRepository extends InMemoryRepository<GoPokedex, String> {

	private PokedexSpecifications pokedexSpecifications;

	/**
	 * Pokedexを変換し、DIに登録する。
	 *
	 * @param pokedexRepository
	 * @param pokemonUtils
	 */
	@Autowired
	@Lazy
	public GoPokedexRepository(
			PokedexRepository pokedexRepository,
			PokemonUtils pokemonUtils,
			PokedexSpecifications pokedexSpecifications) {

		List<Pokedex> pokeList = pokedexRepository.findAll();
		List<GoPokedex> goPokeList = pokeList.stream().map(p -> pokemonUtils.getGoPokedex(p)).collect(Collectors.toList());
		saveAll(goPokeList);

		this.pokedexSpecifications = pokedexSpecifications;

		log.info("GoPokedex table generated!!");
	}

	/**
	 * 主キーはpokedexId
	 */
	@Override
	protected String getKey(GoPokedex t) {
		return t.getPokedexId();
	}

	/**
	 * ポケモン名を部分一致で検索します。<br>
	 * イメージ： WHERE name LIKE '%name%'
	 *
	 * @param name
	 * @return
	 */
	public List<GoPokedex> findByNameIn(Iterable<String> names) {
		List<GoPokedex> goPokedexList = new ArrayList<>();
		records.forEach(gp -> {
			for (String n: names) {
				if (gp.getName().contains(n)) {
					goPokedexList.add((GoPokedex) gp.clone());
					break;
				}
			}
		});
		return goPokedexList;
	}

	/**
	 * 備考を部分一致で検索します。<br>
	 * イメージ： WHERE name LIKE '%name%'
	 *
	 * @param name
	 * @return
	 */
	public List<GoPokedex> findByRemarksIn(Iterable<String> remarks) {
		List<GoPokedex> goPokedexList = new ArrayList<>();
		records.forEach(gp -> {
			for (String n: remarks) {
				if (gp.getRemarks().contains(n)) {
					goPokedexList.add((GoPokedex) gp.clone());
					break;
				}
			}
		});
		return goPokedexList;
	}

	/**
	 * 実装フラグで絞り込んだGoPokedexを取得します。
	 *
	 * @param flg
	 * @return
	 */
	public List<GoPokedex> findByImplFlg(boolean flg) {
		List<GoPokedex> goPokedexList = new ArrayList<>();
		records.forEach(gp -> {
			if (gp.isImplFlg() == flg) {
				goPokedexList.add((GoPokedex) gp.clone());
			}
		});
		return goPokedexList;
	}

	/**
	 * @param type
	 * @return
	 * @see PokedexSpecifications
	 */
	public List<String> findIdByType(TypeEnum type) {
		return pokedexSpecifications.findIdByType(type);
	}


	/**
	 * @param twoTypeKey
	 * @return
	 * @see PokedexSpecifications
	 */
	public List<String> findIdByType(TwoTypeKey twoTypeKey) {
		return pokedexSpecifications.findIdByType(twoTypeKey);
	}


	/**
	 * @param values
	 * @return
	 * @see PokedexSpecifications
	 */
	public List<String> findIdByAny(Map<FilterEnum, FilterParam> values) {
		return pokedexSpecifications.findIdByAny(values);
	}

	/**
	 * @param values
	 * @return
	 * @see PokedexSpecifications
	 */
	public List<GoPokedex> findByAny(Map<FilterEnum, FilterParam> values) {
		List<String> pidList =  pokedexSpecifications.findIdByAny(values);
		return findAllById(pidList);
	}

}
