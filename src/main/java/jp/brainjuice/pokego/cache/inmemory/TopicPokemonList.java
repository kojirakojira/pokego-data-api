package jp.brainjuice.pokego.cache.inmemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.cache.inmemory.data.TopicPokemon;


@Component
public class TopicPokemonList extends ArrayList<TopicPokemon> {

	public TopicPokemonList() {
		super(Collections.synchronizedList(new ArrayList<TopicPokemon>()));
	}

	/**
	 * 使用しない。
	 *
	 * @deprecated
	 */
	@Override
	public boolean add(TopicPokemon e) {
		return super.add(e);
	}

	/**
	 * 使用しない。
	 *
	 * @deprecated
	 */
	@Override
	public boolean addAll(Collection<? extends TopicPokemon> topicPokes) {
		return super.addAll(topicPokes);
	}

	/**
	 * TopicPokemonリストをセットする。
	 * 上限は10件とする。既に追加していたTopicPokemonは削除する。
	 *
	 * @param topicPages
	 * @return
	 */
	public boolean setAll(Collection<TopicPokemon> topicPokes) {

		Collection<? extends TopicPokemon> list = topicPokes;
		if (10 < topicPokes.size()) {
			list = topicPokes.stream()
					.limit(10L) // Listは15件まで。
					.collect(Collectors.toList());
		}
		clear();
		return super.addAll(list);
	}
}
