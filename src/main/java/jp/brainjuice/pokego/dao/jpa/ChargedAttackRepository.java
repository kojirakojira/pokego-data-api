package jp.brainjuice.pokego.dao.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;

import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;

public interface ChargedAttackRepository extends JpaRepository<ChargedAttack, String> {

	/**
	 * ポケモンが覚えるすべての技を取得する。<br>
	 * (=ポケモンが覚えないスペシャル技を省いて取得する。)
	 *
	 * @return
	 */
	@Query(value = "SELECT DISTINCT ca.* "
			+ "FROM charged_attack ca "
			+ "INNER JOIN pokemon_charged_attack pca "
			+ "ON ca.move_id = pca.move_id", nativeQuery = true)
	@Meta(comment = "find chargedAttack all can learn")
	List<ChargedAttack> findAllCanLearn();
}
