package jp.brainjuice.pokego.business.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;

@Repository
public class  GoPokedexRepository implements CrudRepository<GoPokedex, String> {

	private Iterable<GoPokedex> goPokedexes = new ArrayList<>();

	@Override
	@SuppressWarnings("unchecked")
	public <S extends GoPokedex> Iterable<S> saveAll(Iterable<S> entities) {
		goPokedexes = (Iterable<GoPokedex>) entities;
		return (Iterable<S>) goPokedexes;
	}

	@Override
	public Optional<GoPokedex> findById(String id) {
		Optional<GoPokedex> goPokedexOp = Optional.empty();
		for (GoPokedex gp: goPokedexes) {
			if (id.equals(gp.getPokedexId())) {
				goPokedexOp = Optional.of(gp);
				break;
			}
		}
		return goPokedexOp;
	}

	@Override
	public Iterable<GoPokedex> findAll() {
		return goPokedexes;
	}

	public List<GoPokedex> findByNameIn(Iterable<String> name) {
		List<GoPokedex> goPokedexList = new ArrayList<>();
		goPokedexes.forEach(gp -> {
			for (String n: name) {
				if (gp.getName().contains(n)) {
					goPokedexList.add(gp);
					break;
				}
			}
		});
		return goPokedexList;
	}

	public List<GoPokedex> findAll(Iterable<String> nameParts) {
		List<GoPokedex> goPokedexList = new ArrayList<>();
		goPokedexes.forEach(gp -> {
			for (String n: nameParts) {
				if (gp.getName().contains(n)) {
					goPokedexList.add(gp);
					break;
				}
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
	public Iterable<GoPokedex> findAllById(Iterable<String> ids) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
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
