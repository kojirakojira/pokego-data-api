package jp.brainjuice.pokego.cache.dao;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.cache.dao.entity.PokemonTempView;

public interface PokemonTempViewRedisRepository extends CrudRepository<PokemonTempView, String> {

}
