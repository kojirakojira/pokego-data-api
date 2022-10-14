package jp.brainjuice.pokego.cache.dao;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.cache.dao.entity.PokemonTempView;

/**
 *
 * Redis上の一時的なポケモン閲覧情報を管理するためのリポジトリ
 *
 * @author saibabanagchampa
 * @see PokemonTempView
 *
 */
public interface PokemonTempViewRedisRepository extends CrudRepository<PokemonTempView, String> {

}
