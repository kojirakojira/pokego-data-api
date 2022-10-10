package jp.brainjuice.pokego.cache.dao.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RedisHash("pokemon-view-info")
public class PokemonViewInfo {

	@Id
	private String pokedexId;
	private String ip;
	private Date time;
}
