package jp.brainjuice.pokego.cache.dao.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RedisHash("page-view-info")
public class PageViewInfo {

	@Id
	/** =SearchPattern */
	private String page;
	private String ip;
	private Date time;
}
