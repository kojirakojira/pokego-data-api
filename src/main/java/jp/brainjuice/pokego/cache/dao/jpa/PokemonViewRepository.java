package jp.brainjuice.pokego.cache.dao.jpa;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.brainjuice.pokego.cache.dao.postgres.entity.PokemonView;
import jp.brainjuice.pokego.cache.inmemory.topic.data.PageNameEnum;

/**
 * ページごとの閲覧数
 *
 * @see PageNameEnum
 */
public interface PokemonViewRepository extends JpaRepository<PokemonView, Integer> {

	/**
	 * 今日の閲覧数のレコードを取得する。
	 *
	 * @param strDate yyyy-MM-dd
	 * @return
	 */
	@Query(value = "SELECT * FROM pokemon_view WHERE DATE(ymd) = :date", nativeQuery = true)
	@Meta(comment = "find all by ymd(pokemon_view)")
	List<PokemonView> findAllByYmd(@Param("date") Date date);

	/**
	 * 指定したpokedex_idの閲覧数をすべて取得する。
	 *
	 * @param pid
	 * @return
	 */
	@Query(value = "SELECT * FROM pokemon_view WHERE pokedex_id = :pid", nativeQuery = true)
	@Meta(comment = "find all by pid")
	List<PokemonView> findAllByPid(@Param("pid") String pid);
}
