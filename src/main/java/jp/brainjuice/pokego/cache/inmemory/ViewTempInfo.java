package jp.brainjuice.pokego.cache.inmemory;

import java.util.Date;

import jp.brainjuice.pokego.cache.inmemory.data.PageNameEnum;
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
