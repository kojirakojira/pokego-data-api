package jp.brainjuice.pokego.dao.redis.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import jp.brainjuice.pokego.cache.BjRedisEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 種族値比較の検索履歴情報<br>
 * Redisサーバ上に保存するためのBeanクラス。<br>
 * 3日で期限切れにする。
 *
 * @author saibabanagchampa
 * @see BjRedisEnum.raceDiffSearchTempView
 *
 */
@Data
@AllArgsConstructor
@RedisHash(value = "raceDiffSearchTempView", timeToLive = 259200L)
public class RaceDiffSearchTempView implements TempView {

	/** id = (searchHash + UUID) */
	@Id
	private String id;
	/** =searchHash */
	private String key;
	private String ip;
	private Date viewTime;
}
