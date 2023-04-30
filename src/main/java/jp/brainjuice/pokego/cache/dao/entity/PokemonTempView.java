package jp.brainjuice.pokego.cache.dao.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ポケモンの閲覧情報<br>
 * Redisサーバ上に保存するためのBeanクラス。<br>
 * 3日で期限切れにする。（過去3日間での閲覧数を求めるために使用する。）
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
@RedisHash(value = "pokemonTempView", timeToLive = 259200L)
public class PokemonTempView implements TempView {

	/** id = (pokedexId + UUID) */
	@Id
	private String id;
	/** =pokedexId */
	private String key;
	private String ip;
	private Date viewTime;
}
