package jp.brainjuice.pokego.cache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.cache.inmemory.TopicPageList;
import jp.brainjuice.pokego.cache.inmemory.TopicPokemonList;

@Service
public class TopicListProvider {

	private TopicListManager topicListManager;

	@Autowired
	public TopicListProvider(TopicListManager topicListManager) {
		this.topicListManager = topicListManager;
	}

	/**
	 * 話題のページ（検索パターン）を取得する。
	 *
	 * @return
	 */
	public TopicPageList getTopicPageList() {
		return topicListManager.getTopicPageList();
	}

	/**
	 * 話題のポケモンを取得する。
	 *
	 * @return
	 */
	public TopicPokemonList getTopicPokemonList() {
		return topicListManager.getTopicPokemonList();
	}

	/**
	 * TopicListを強制的に更新させる。（普通はスケジュール実行）
	 *
	 */
	public void updateTopicList() {
		topicListManager.updateTopicList();
	}
}
