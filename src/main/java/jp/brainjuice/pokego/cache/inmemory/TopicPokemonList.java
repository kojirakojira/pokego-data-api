package jp.brainjuice.pokego.cache.inmemory;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.cache.inmemory.data.TopicPokemon;


@Component
public class TopicPokemonList extends ArrayList<TopicPokemon> {

	public TopicPokemonList() {
		super(Collections.synchronizedList(new ArrayList<TopicPokemon>()));
	}
}
