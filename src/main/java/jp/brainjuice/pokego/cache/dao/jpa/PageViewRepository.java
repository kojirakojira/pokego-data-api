package jp.brainjuice.pokego.cache.dao.jpa;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.brainjuice.pokego.cache.dao.jpa.entity.PageView;
import jp.brainjuice.pokego.cache.inmemory.topic.data.PageNameEnum;

/**
 * ページごとの閲覧数
 *
 * @see PageNameEnum
 */
public interface PageViewRepository extends JpaRepository<PageView, Integer> {

	/**
	 * 今日の閲覧数のレコードを取得する。
	 *
	 * @param strDate yyyy-MM-dd
	 * @return
	 */
	@Query(value = "SELECT * FROM page_view WHERE DATE(ymd) = :date", nativeQuery = true)
	@Meta(comment = "find all by ymd(page_view)")
	List<PageView> findAllByYmd(@Param("date") Date date);

	/**
	 * 指定したpageの閲覧数をすべて取得する。
	 *
	 * @param pid
	 * @return
	 */
	@Query(value = "SELECT * FROM page_view WHERE page = :page", nativeQuery = true)
	@Meta(comment = "find all by page")
	List<PageView> findAllByPage(@Param("page") String page);
}
