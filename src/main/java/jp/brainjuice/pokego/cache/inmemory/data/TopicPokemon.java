package jp.brainjuice.pokego.cache.inmemory.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopicPokemon {

	private String page;
	private String name;
	private String image;
	private int count;
}
