package jp.brainjuice.pokego.business.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * メモリにて保持しているGoPokedexを管理するRepositoryです。<br>
 * Spring Data Jpaに近い仕様を目指してますが、ほぼハリボテです。
 *
 * @author saibabanagchampa
 *
 */
@Repository
@Slf4j
public class  GoPokedexRepository implements CrudRepository<GoPokedex, String> {

	private final List<GoPokedex> goPokedexes = new ArrayList<>();

	/**
	 * Pokedexを変換し、DIに登録する。
	 *
	 * @param pokedexRepository
	 * @param pokemonUtils
	 */
	@Autowired
	public GoPokedexRepository(
			PokedexRepository pokedexRepository,
			PokemonUtils pokemonUtils) {

		List<Pokedex> pokeList = pokedexRepository.findAll();
		List<GoPokedex> goPokeList = pokeList.stream().map(p -> pokemonUtils.getGoPokedex(p)).collect(Collectors.toList());
		saveAll(goPokeList);

		log.info("GoPokedex table generated!!");
	}

	@Override
	public <S extends GoPokedex> Iterable<S> saveAll(Iterable<S> entities) {
		entities.forEach(goPokedexes::add);
		return entities;
	}

	@Override
	public Optional<GoPokedex> findById(String id) {
		Optional<GoPokedex> goPokedexOp = Optional.empty();
		for (GoPokedex gp: goPokedexes) {
			if (id.equals(gp.getPokedexId())) {
				goPokedexOp = Optional.of(gp.clone());
				break;
			}
		}
		return goPokedexOp;
	}

	@Override
	public List<GoPokedex> findAll() {
		return new ArrayList<>(goPokedexes);
	}

	@Override
	public List<GoPokedex> findAllById(Iterable<String> ids) {
		return StreamSupport.stream(ids.spliterator(), false).map(pid -> {
			for (GoPokedex p: goPokedexes) {
				if (p.getPokedexId().equals(pid)) {
					return p;
				}
			}
			return null;
		}).collect(Collectors.toList());
	}

	/**
	 * 部分一致で検索します。<br>
	 * イメージ： WHERE name LIKE '%name%'
	 *
	 * @param name
	 * @return
	 */
	public List<GoPokedex> findByNameIn(Iterable<String> name) {
		List<GoPokedex> goPokedexList = new ArrayList<>();
		goPokedexes.forEach(gp -> {
			for (String n: name) {
				if (gp.getName().contains(n)) {
					goPokedexList.add(gp.clone());
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
		goPokedexes.forEach(gp -> {
			if (gp.isImplFlg() == flg) {
				goPokedexList.add(gp.clone());
			}
		});
		return goPokedexList;
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public <S extends GoPokedex> S save(S entity) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public boolean existsById(String id) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public long count() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteById(String id) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void delete(GoPokedex entity) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAllById(Iterable<? extends String> ids) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAll(Iterable<? extends GoPokedex> entities) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAll() {
		// TODO 自動生成されたメソッド・スタブ

	}

}
