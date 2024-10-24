package jp.brainjuice.pokego.cache.inmemory.topic;

import java.util.Date;

import jp.brainjuice.pokego.cache.inmemory.topic.data.PageNameEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ViewTempInfo {

	private PageNameEnum page;
	private String pokedexId;
	private String ip;
	/** 閲覧した時間 */
	private Date time;
}
