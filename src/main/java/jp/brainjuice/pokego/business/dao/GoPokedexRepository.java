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

	/**
	 * リストのポケモン名から検索
	 *
	 * @param name
	 * @return
	 */
	@Meta(comment = "find goPokedex by name like in")
	@Query(value = "SELECT * FROM go_pokedex gp WHERE name LIKE ANY(:names)", nativeQuery = true)
	List<GoPokedex> findByNameLikeIn(String[] names);

	/**
	 * 備考を部分一致で検索
	 *
	 * @param name
	 * @return
	 */
	@Meta(comment = "find by remarks containing")
	@Query(value = "SELECT * FROM go_pokedex WHERE remarks LIKE ANY(:remarks)", nativeQuery = true)
	List<GoPokedex> findByRemarksContaining(String[] remarks);

	/**
	 * 実装フラグで絞り込んだGoPokedexを取得
	 *
	 * @param flg
	 * @return
	 */
	@Meta(comment = "find by implFlg")
	List<GoPokedex> findByImplFlg(boolean flg);

	/**
	 * タイプが一致するポケモンのpokedexIdを取得
	 * @param type
	 * @return
	 * @see PokedexSpecifications
	 */
	@Query("SELECT gp.pokedexId FROM GoPokedex gp WHERE gp.type1 = :type OR gp.type2 = :type")
	@Meta(comment = "find id by type")
	List<String> findIdByType(@Param("type") TypeEnum type);

	/**
	 * 2タイプが一致するポケモンのpokedexIdを取得
	 * @param type
	 * @return
	 */
	@Query("SELECT gp.pokedexId"
			+ " FROM GoPokedex gp"
			+ " WHERE (gp.type1 = :#{#type.type1} AND gp.type2 = :#{#type.type2})"
			+ " OR (gp.type1 = :#{#type.type2} AND gp.type2 = :#{#type.type1})")
	@Meta(comment = "find id by type")
	List<String> findIdByType(@Param("type") TwoTypeKey type);


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
