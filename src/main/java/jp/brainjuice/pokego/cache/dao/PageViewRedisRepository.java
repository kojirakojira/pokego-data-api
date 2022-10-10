package jp.brainjuice.pokego.cache.dao;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.cache.dao.entity.PageViewInfo;

public interface PageViewRedisRepository extends CrudRepository<PageViewInfo, String> {

}
