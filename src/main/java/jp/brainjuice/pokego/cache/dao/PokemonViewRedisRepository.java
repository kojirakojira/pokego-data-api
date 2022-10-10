package jp.brainjuice.pokego.cache.dao;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.cache.dao.entity.PokemonViewInfo;

public interface PokemonViewRedisRepository extends CrudRepository<PokemonViewInfo, String> {

}
