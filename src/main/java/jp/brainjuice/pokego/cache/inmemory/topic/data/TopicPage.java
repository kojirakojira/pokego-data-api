package jp.brainjuice.pokego.cache.inmemory.topic.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopicPage {

	private PageNameEnum page;
	private String name;
	private int count;

}
