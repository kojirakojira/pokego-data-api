package jp.brainjuice.pokego.business.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;

/**
 * ポケモンGOにおけるポケモンの情報を取得するRepositoryクラス（go_pokedexはMView）
 *
 * @author saibabanagchampa
 *
 */
@Repository
public interface GoPokedexRepository extends JpaRepository<GoPokedex, String>, JpaSpecificationExecutor<GoPokedex> {


//	private PokedexSpecifications pokedexSpecifications;
//
//	/**
//	 * Pokedexを変換し、DIに登録する。
//	 *
//	 * @param pokedexRepository
//	 * @param pokemonUtils
//	 */
//	@Lazy
//	public GoPokedexRepository(
//			PokedexRepository pokedexRepository,
//			PokemonUtils pokemonUtils,
//			PokedexSpecifications pokedexSpecifications) {
//
//		List<Pokedex> pokeList = pokedexRepository.findAll();
//		List<GoPokedex> goPokeList = pokeList.stream().map(p -> pokemonUtils.getGoPokedex(p)).collect(Collectors.toList());
//		saveAll(goPokeList);
//
//		this.pokedexSpecifications = pokedexSpecifications;
//
//		log.info("GoPokedex table generated!! (Referenced file: none.)");
//	}
//
//	/**
//	 * 主キーはpokedexId
//	 */
//	@Override
//	protected String getKey(GoPokedex t) {
//		return t.getPokedexId();
//	}
//
	/**
	 * リスト状のポケモン名から検索します。<br>
	 * パターンマッチで指定することにより、部分一致での検索が可能です。<br>
	 * イメージ： WHERE name LIKE IN ('name1', 'name2'
	 *
	 * @param name
	 * @return
	 */
	@Meta(comment = "find goPokedex by name like in")
	@Query(value = "SELECT * FROM go_pokedex gp WHERE name ~~* :names", nativeQuery = true)
	List<GoPokedex> findByNameLikeIn(Iterable<String> names);
//
//	/**
//	 * 備考を部分一致で検索します。<br>
//	 * イメージ： WHERE name LIKE '%name%'
//	 *
//	 * @param name
//	 * @return
//	 */
//	public List<GoPokedex> findByRemarksIn(Iterable<String> remarks) {
//		return records.stream()
//				.filter(gp -> {
//					for (String n: remarks) {
//						if (gp.getRemarks().contains(n)) {
//							return true;
//						}
//					}
//					return false;
//				})
//				.map(gp -> (GoPokedex) gp.clone())
//				.collect(Collectors.toList());
//	}
	@Meta(comment = "find by remarks containing")
	@Query(value = "SELECT * FROM go_pokedex WHERE remarks ~~* any(:remarks)", nativeQuery = true)
	List<GoPokedex> findByRemarksContaining(List<String> remarks);
//
//	/**
//	 * 実装フラグで絞り込んだGoPokedexを取得します。
//	 *
//	 * @param flg
//	 * @return
//	 */
//	public List<GoPokedex> findByImplFlg(boolean flg) {
//		List<GoPokedex> goPokedexList = new ArrayList<>();
//		records.forEach(gp -> {
//			if (gp.isImplFlg() == flg) {
//				goPokedexList.add((GoPokedex) gp.clone());
//			}
//		});
//		return goPokedexList;
//	}
	@Meta(comment = "find by implFlg")
	List<GoPokedex> findByImplFlg(boolean flg);
//
//	/**
//	 * @param type
//	 * @return
//	 * @see PokedexSpecifications
//	 */
//	public List<String> findIdByType(TypeEnum type) {
//		return pokedexSpecifications.findIdByType(type);
//	}
	@Query("SELECT gp.pokedexId FROM GoPokedex gp WHERE gp.type1 = :type OR gp.type2 = :type")
	@Meta(comment = "find id by type")
	List<String> findIdByType(@Param("type") TypeEnum type);
//
//
//	/**
//	 * @param twoTypeKey
//	 * @return
//	 * @see PokedexSpecifications
//	 */
//	public List<String> findIdByType(TwoTypeKey twoTypeKey) {
//		return pokedexSpecifications.findIdByType(twoTypeKey);
//	}
	@Query("SELECT gp.pokedexId"
			+ " FROM GoPokedex gp"
			+ " WHERE (gp.type1 = :#{#type.type1} AND gp.type2 = :#{#type.type2})"
			+ " OR (gp.type1 = :#{#type.type2} AND gp.type2 = :#{#type.type1})")
	@Meta(comment = "find id by type")
	List<String> findIdByType(@Param("type") TwoTypeKey type);
//
//
//	/**
//	 * @param values
//	 * @return
//	 * @see PokedexSpecifications
//	 */
//	public List<String> findIdByAny(Map<FilterEnum, FilterParam> values) {
//		return pokedexSpecifications.findIdByAny(values);
//	}
//
//	/**
//	 * @param values
//	 * @return
//	 * @see PokedexSpecifications
//	 */
//	public List<GoPokedex> findByAny(Map<FilterEnum, FilterParam> values) {
//		List<String> pidList =  pokedexSpecifications.findIdByAny(values);
//		return findAllById(pidList);
//	}

	@Query("SELECT gp.implFlg FROM GoPokedex gp WHERE gp.pokedexId = :pid")
	@Meta(comment = "find implFlg by Id")
	boolean findImplFlgById(@Param("pid") String pid);



    @Override
    @Deprecated
    default void delete(GoPokedex entity) {
        throw new UnsupportedOperationException("Delete operation is not supported for materialized view.");
    }

    @Override
    @Deprecated
    default void deleteById(String id) {
        throw new UnsupportedOperationException("Delete operation is not supported for materialized view.");
    }

    /**
     * MViewの更新
     */
    @Query(value = "REFRESH MATERIALIZED VIEW go_pokedex", nativeQuery = true)
    @Modifying
    @Transactional
    void refresh();
}
