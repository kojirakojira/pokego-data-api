package jp.brainjuice.pokego.cache.dao.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ページの閲覧情報<br>
 * 3時間で期限切れにする。（過去3時間での閲覧数を求めるために使用する。）
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
@RedisHash(value = "pageTempView", timeToLive = 10800L)
public class PageTempView {

	/** id = (page + UUID) */
	@Id
	private String id;
	/** =SearchPattern */
	private String page;
	private String ip;
	private Date viewTime;
}
