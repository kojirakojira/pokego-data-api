package jp.brainjuice.pokego.cache.dao;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.cache.dao.entity.PageTempView;

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
