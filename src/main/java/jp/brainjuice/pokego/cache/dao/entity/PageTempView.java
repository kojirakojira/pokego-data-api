package jp.brainjuice.pokego.cache.dao.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ページの閲覧情報<br>
 * Redisサーバ上に保存するためのBeanクラス。<br>
 * 3日で期限切れにする。（過去3日間での閲覧数を求めるために使用する。）
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
@RedisHash(value = "pageTempView", timeToLive = 259200L)
public class PageTempView implements TempView {

	/** id = (page + UUID) */
	@Id
	private String id;
	/** =page =SearchPattern */
	private String key;
	private String ip;
	private Date viewTime;
}
