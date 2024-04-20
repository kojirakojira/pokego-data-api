package jp.brainjuice.pokego.business.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.business.dao.entity.Entity;

/**
 * サーバ代節約のため、更新が発生しない軽量な情報はインメモリで保持する。
 * Repositoryクラスとして管理した方が取り回しが良いため、
 * Springにおける一般的なRepository連携と遜色ないような機能を目指している。
 * だが、ハリボテ感は否めない。
 *
 * @author saibabanagchampa
 *
 * @param <T>
 * @param <ID>
 */
public abstract class InMemoryRepository<T extends Entity, ID> implements CrudRepository<T, ID>  {

	protected final List<T> records = new ArrayList<>();

	protected abstract ID getKey(T t);

	/**
	 * 図鑑№からPokedexを検索します。
	 *
	 * @param pokedexId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Optional<T> findById(ID id) {

		Optional<T> entityOp = Optional.empty();

		if (id == null) return entityOp;

		for (T t: records) {
			if (id.equals(getKey(t))) {
				entityOp = Optional.of((T) t.clone());
				break;
			}
		}

		return entityOp;
	}

	/**
	 * すべてのPokedexを取得します。
	 *
	 * @return
	 */
	public List<T> findAll() {
		return new ArrayList<>(records);
	}

	/**
	 * 図鑑№のリストからPokedexを検索します。
	 *
	 * @param ids
	 * @return
	 */
	@Override
	public List<T> findAllById(Iterable<ID> ids) {
		return StreamSupport.stream(ids.spliterator(), false).map(ID -> {
			for (T t: records) {
				if (getKey(t).equals(ID)) {
					return t;
				}
			}
			return null;
		}).collect(Collectors.toList());

	}

	@Override
	public long count() {
		return records.size();
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void delete(T entity) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAll() {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAll(Iterable<? extends T> entities) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAllById(Iterable<? extends ID> ids) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteById(ID id) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public boolean existsById(ID id) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public <S extends T> S save(S entity) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/**
	 * すべてのPokedexを保存します。
	 *
	 * @param <S>
	 * @param records
	 * @return
	 */
	@Override
	public <S extends T> Iterable<S> saveAll(Iterable<S> e) {
		e.forEach(records::add);
		return e;
	}
}
