package jp.brainjuice.pokego.cache.dao.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ポケモンの閲覧情報<br>
 * 3時間で期限切れにする。（過去3時間での閲覧数を求めるために使用する。）
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
@RedisHash(value = "pokemonTempView", timeToLive = 10800L)
public class PokemonTempView implements TempView {

	/** id = (pokedexId + UUID) */
	@Id
	private String id;
	/** =pokedexId */
	private String key;
	private String ip;
	private Date viewTime;
}
