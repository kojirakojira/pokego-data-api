package jp.brainjuice.pokego.dao.redis;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.dao.redis.entity.LastUpdate;

/**
 *
 * 最終更新日のリポジトリ
 *
 * @author saibabanagchampa
 *
 */
public interface LastUpdateRepository extends CrudRepository<LastUpdate, String> {

}

