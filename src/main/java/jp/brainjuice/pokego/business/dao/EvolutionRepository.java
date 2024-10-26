package jp.brainjuice.pokego.business.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.brainjuice.pokego.business.dao.dto.BasePokedexNo;
import jp.brainjuice.pokego.business.dao.entity.Evolution;
import jp.brainjuice.pokego.business.dao.entity.EvolutionPk;

public interface EvolutionRepository extends JpaRepository<Evolution, EvolutionPk> {

	@Query("SELECT e.evolAnnotations"
			+ " FROM Evolution e"
			+ " WHERE e.evolAnnotations IS NOT NULL"
			+ " AND e.pokedexId IN (:pids)")
	@Meta(comment = "find evol_annotations by id in")
	List<String> findEvolAnnotationsByIdIn(@Param("pids") Iterable<String> pids);

	/**
	 * 進化前のポケモンのpokedexIdを取得する。
	 * ガーメイルのような進化前が複数存在するポケモンの場合は複数要素返却される。
	 *
	 * @param pid
	 * @return
	 */
	@Query("SELECT e.beforePokedexId FROM Evolution e WHERE e.pokedexId = :pid")
	@Meta(comment = "find bid by id")
	List<String> findBidById(@Param("pid") String pid);


	/**
	 * 進化後のポケモンのpokedexIdを取得する。
	 *
	 * @param pid
	 * @return
	 */
	@Query("SELECT e.pokedexId FROM Evolution e WHERE e.beforePokedexId = :bid")
	@Meta(comment = "find id by bid")
	List<String> findIdByBid(@Param("bid") String bid);


	/**
	 * 別のすがたのpokedexIdを取得する。
	 *
	 * @param pid
	 * @return
	 */
	@Query("SELECT e.pokedexId"
			+ " FROM Evolution e"
			+ " WHERE e.pokedexId <> :pid"
			+ " AND e.pokedexId LIKE substring(:pid, 1, 4) || '%'")
	@Meta(comment = "find anoForm by id")
	List<String> findAnoFormById(@Param("pid") String pid);

	/**
	 * IN句を使用し、指定したキーのEvolutionを取得する。
	 *
	 * @param pks
	 * @return
	 */
	@Query(value = "SELECT * FROM evolution WHERE (pokedex_id, before_pokedex_id) IN (:pks)", nativeQuery = true)
	@Meta(comment = "find by id in")
	List<Evolution> findByIdIn(@Param("pks") List<EvolutionPk> pks);

	/**
	 * 進化ツリー上の最初のポケモンを取得する。
	 * ガーメイルのような進化前が複数存在するポケモンの場合は複数要素返却される。
	 *
	 * @param pid
	 * @return
	 */
	@Query(value = "WITH RECURSIVE root AS ("
			+ "  SELECT pokedex_id, before_pokedex_id FROM evolution WHERE pokedex_id = :pid"
			+ "  UNION ALL"
			+ "  SELECT evol.pokedex_id, evol.before_pokedex_id"
			+ "    FROM evolution evol"
			+ "    INNER JOIN root r ON evol.pokedex_id = r.before_pokedex_id"
			+ ")"
			+ "SELECT pokedex_id FROM root WHERE before_pokedex_id = 'root'", nativeQuery = true)
	@Meta(comment = "find root by id")
	List<String> findRootById(@Param("pid") String pid);


	/**
	 * 指定したポケモンの最終進化をすべて取得する。（メガシンカは含まない。）
	 *
	 * @param pid
	 * @return
	 */
	@Query(value = "WITH RECURSIVE tree(i, pokedex_id, before_pokedex_id) AS ("
			+ "  SELECT 0, pokedex_id, before_pokedex_id"
			+ "    FROM evolution "
			+ "    WHERE pokedex_id = :pid"
			+ "  UNION ALL"
			+ "  SELECT i + 1, evol.pokedex_id, evol.before_pokedex_id"
			+ "    FROM evolution evol"
			+ "    INNER JOIN tree t "
			+ "    ON evol.before_pokedex_id = t.pokedex_id"
			+ ")"
			+ "SELECT pokedex_id"
			+ "  FROM tree t "
			+ "  WHERE t.i = (SELECT MAX(i) FROM tree t2)", nativeQuery = true)
	@Meta(comment = "find leaf by id")
	List<String> findLeafById(@Param("pid") String pid);


	@Query(value = "WITH RECURSIVE tree AS ("
			+ "  SELECT pokedex_id, before_pokedex_id"
			+ "    FROM evolution"
			+ "    WHERE pokedex_id IN ("
			+ "      WITH RECURSIVE root AS ("
			+ "        SELECT pokedex_id, before_pokedex_id"
			+ "          FROM evolution"
			+ "          WHERE pokedex_id = :pid"
			+ "        UNION ALL"
			+ "        SELECT evol.pokedex_id, evol.before_pokedex_id"
			+ "           FROM evolution evol"
			+ "           INNER JOIN root r"
			+ "           ON evol.pokedex_id = r.before_pokedex_id"
			+ "      )"
			+ "      SELECT pokedex_id FROM root WHERE before_pokedex_id = 'root'"
			+ "    )"
			+ "  UNION ALL"
			+ "  SELECT evol2.pokedex_id, evol2.before_pokedex_id"
			+ "    FROM evolution evol2"
			+ "    INNER JOIN tree t"
			+ "    ON evol2.before_pokedex_id = t.pokedex_id"
			+ "  )"
			+ "  SELECT * FROM tree", nativeQuery = true)
	@Meta(comment = "find evol tree pk by id")
	List<Object[]> getEvolTreePkByIdRaw(@Param("pid") String pid);
	/**
	 * 指定したpokedexIdの進化ツリーを取得する。<br>
	 * 進化ツリー全体のpokedexId, beforePokedexIdを取得する。
	 *
	 * @param pid
	 * @return
	 */
	default List<EvolutionPk> getEvolTreePkById(String pid) {
		return getEvolTreePkByIdRaw(pid).stream()
				.map(objs -> new EvolutionPk((String) objs[0], (String) objs[1]))
				.toList();
	}

	/**
	 * 指定したpokedexIdの進化ツリーを取得する。<br>
	 * ガラルニャースの場合、ガラルニャースとニャイキングを取得する。
	 *
	 * @param pid
	 * @return
	 */
	@Query(value = "WITH RECURSIVE tree AS ("
			+ "  SELECT pokedex_id, before_pokedex_id"
			+ "    FROM evolution"
			+ "    WHERE pokedex_id IN ("
			+ "      WITH RECURSIVE root AS ("
			+ "        SELECT pokedex_id, before_pokedex_id"
			+ "          FROM evolution"
			+ "          WHERE pokedex_id = :pid"
			+ "        UNION ALL"
			+ "        SELECT evol.pokedex_id, evol.before_pokedex_id"
			+ "           FROM evolution evol"
			+ "           INNER JOIN root r"
			+ "           ON evol.pokedex_id = r.before_pokedex_id"
			+ "      )"
			+ "      SELECT pokedex_id FROM root WHERE before_pokedex_id = 'root'"
			+ "    )"
			+ "  UNION ALL"
			+ "  SELECT evol2.pokedex_id, evol2.before_pokedex_id"
			+ "    FROM evolution evol2"
			+ "    INNER JOIN tree t"
			+ "    ON evol2.before_pokedex_id = t.pokedex_id"
			+ ")"
			+ "SELECT e.* FROM tree"
			+ "  INNER JOIN evolution e"
			+ "  ON tree.pokedex_id = e.pokedex_id"
			+ "  AND tree.before_pokedex_id = e.before_pokedex_id", nativeQuery = true)
	@Meta(comment = "get evol tree by id")
	List<Evolution> getEvolTreeById(@Param("pid") String pid);


	/**
	 * 1系統のすべてのポケモンを取得する。<br>
	 * ニャースの場合、ニャース（全リージョン）、ペルシアン、ニャイキング。全て。
	 * rootのポケモンの図鑑Noを取得して、そこからleaf側に探索していく。
	 *
	 * @param pid
	 * @return
	 */
	@Query(value = "WITH RECURSIVE tree AS ("
			+ "  SELECT pokedex_id, before_pokedex_id FROM evolution WHERE pokedex_id ~~* any("
			+ "    ("
			+ "      WITH RECURSIVE root AS ("
			+ "        SELECT pokedex_id, before_pokedex_id FROM evolution WHERE pokedex_id = :pid"
			+ "        UNION ALL"
			+ "        SELECT evo.pokedex_id, evo.before_pokedex_id"
			+ "          FROM evolution evo"
			+ "          INNER JOIN root r ON evo.pokedex_id = r.before_pokedex_id"
			+ "    )"
			+ "    SELECT DISTINCT substring(pokedex_id, 1, 4) || '%' FROM root WHERE before_pokedex_id = 'root'"
			+ "  ))"
			+ "  UNION ALL"
			+ "  SELECT evo2.pokedex_id, evo2.before_pokedex_id"
			+ "    FROM evolution evo2"
			+ "    INNER JOIN tree t"
			+ "    ON evo2.before_pokedex_id = t.pokedex_id"
			+ ")"
			+ "SELECT e.*"
			+ "  FROM tree t"
			+ "  INNER JOIN evolution e"
			+ "  ON t.pokedex_id = e.pokedex_id"
			+ "  AND t.before_pokedex_id = e.before_pokedex_id", nativeQuery = true)
	@Meta(comment = "get lineage by id")
	List<Evolution> getLineageById(@Param("pid") String pid);


	@Query(value = "WITH RECURSIVE root AS ("
			+ "  SELECT pokedex_id, before_pokedex_id, pokedex_id AS t_pid FROM evolution"
			+ "  UNION ALL"
			+ "  SELECT evol.pokedex_id, evol.before_pokedex_id, r.t_pid "
			+ "    FROM evolution evol"
			+ "    INNER JOIN root r ON evol.pokedex_id = r.before_pokedex_id "
			+ ")"
			+ "SELECT DISTINCT t_pid AS target_pokedex_id, substring(pokedex_id, 1, 4)::integer AS pokedex_no"
			+ "  FROM root"
			+ "  WHERE before_pokedex_id = 'root'", nativeQuery = true)
	@Meta(comment = "find all base pokedex no raw")
	List<Object[]> findAllBasePokedexNoRaw();
	/**
	 * 図鑑IDに対応する、第一形態の図鑑Noを取得する。
	 * @return
	 */
	default List<BasePokedexNo> findAllBasePokedexNo() {
		return findAllBasePokedexNoRaw()
				.stream()
				.map(BasePokedexNo::new)
				.toList();
	};

}
