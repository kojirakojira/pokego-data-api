package jp.brainjuice.pokego.cache.dao;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.cache.dao.entity.PageTempView;

public interface PageTempViewRedisRepository extends CrudRepository<PageTempView, String> {

}
