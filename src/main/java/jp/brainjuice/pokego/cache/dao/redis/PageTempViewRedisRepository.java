package jp.brainjuice.pokego.cache.dao.redis;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.cache.dao.redis.entity.PageTempView;

/**
 *
 * Redis上の一時的なページ閲覧情報を管理するためのリポジトリ
 *
 * @author saibabanagchampa
 * @see PageTempView
 *
 */
public interface PageTempViewRedisRepository extends CrudRepository<PageTempView, String> {

}
