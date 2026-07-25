package jp.brainjuice.pokego.dao.redis;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.dao.redis.entity.RaceDiffSearchTempView;

/**
 *
 * Redis上の一時的な種族値比較の検索履歴を管理するためのリポジトリ
 *
 * @author saibabanagchampa
 * @see RaceDiffSearchTempView
 *
 */
public interface RaceDiffSearchTempViewRedisRepository extends CrudRepository<RaceDiffSearchTempView, String> {

}
