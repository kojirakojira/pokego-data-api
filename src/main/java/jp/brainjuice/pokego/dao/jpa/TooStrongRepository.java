package jp.brainjuice.pokego.dao.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import jp.brainjuice.pokego.dao.jpa.entity.TooStrong;

public interface TooStrongRepository extends JpaRepository<TooStrong, String> {

	/**
	 * MViewの更新
	 */
	@Query(value = "REFRESH MATERIALIZED VIEW too_strong", nativeQuery = true)
	@Modifying
	@Transactional
	void refresh();
}
