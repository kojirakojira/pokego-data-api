package jp.brainjuice.pokego.cache.inmemory.topic;

import java.util.Date;

import java.util.List;

import jp.brainjuice.pokego.cache.inmemory.topic.data.PageNameEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ViewTempInfo {

	private PageNameEnum page;
	private List<String> pokedexIds;
	private String ip;
	/** 閲覧した時間 */
	private Date time;
}
